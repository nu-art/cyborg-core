package com.nu.art.cyborg.io.transceiver;

import android.os.Handler;

import com.nu.art.belog.Logger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.modules.ThreadsModule;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BaseTransceiver
		extends Logger {

	protected SocketWrapper socket;

	//		protected BluetoothSocket socket;
	private ConnectionState state = ConnectionState.Idle;

	protected final String name;

	protected final PacketSerializer packetSerializer;

	private final Handler receiver;

	private final Handler transmitter;

	private boolean listen = true;

	private TransceiverListener[] listeners = new TransceiverListener[0];

	private Runnable connectAndListen = new Runnable() {
		@Override
		public void run() {
			try {
				setState(ConnectionState.Connecting);
				socket = connectImpl();
				setState(ConnectionState.Connected);

				while (listen) {
					try {
						processPacket();
					} catch (Exception e) {
						notifyError(e);
					}
				}
			} catch (Exception e) {
				notifyError(e);
			}
		}
	};

	public BaseTransceiver(String name, PacketSerializer packetSerializer) {
		this.name = name;
		setTag(name);
		this.packetSerializer = packetSerializer;
		transmitter = CyborgBuilder.getModule(ThreadsModule.class).getDefaultHandler("Transmitter - " + name);
		receiver = CyborgBuilder.getModule(ThreadsModule.class).getDefaultHandler("Receiver - " + name);
		CyborgBuilder.getInstance().setBeLogged(this);
	}

	public final void sendPacket(final Packet packet) {
		transmitter.post(new Runnable() {
			@Override
			public void run() {
				if (socket == null) {
					logWarning("Socket is null ignoring packet: " + packet);
					return;
				}

				logInfo("Sending packet to remote device: " + packet);
				try {
					OutputStream os = socket.getOutputStream();
					packetSerializer.serializePacket(os, packet);
				} catch (IOException e) {
					notifyError(e);
				}
			}
		});
	}

	public void connect() {
		receiver.removeCallbacks(connectAndListen);
		receiver.post(connectAndListen);
	}

	protected void processPacket()
			throws IOException {
		notifyNewPacket(packetSerializer.extractPacket(socket.getInputStream()));
	}

	public final void addListener(TransceiverListener listener) {
		listeners = ArrayTools.appendElement(listeners, listener);
	}

	public final void removeListener(TransceiverListener incomingPacketListener) {
		listeners = ArrayTools.removeElement(listeners, incomingPacketListener);
	}

	protected ConnectionState getState() {
		return state;
	}

	public final void setState(ConnectionState newState) {
		if (state == newState)
			return;

		logDebug("State changed: " + state + " ==> " + newState);
		this.state = newState;
		notifyStateChanged(newState);
	}

	protected abstract SocketWrapper connectImpl()
			throws Exception;

	public void disconnect() {
		setState(ConnectionState.Disconnecting);
		listen = false;

		if (socket == null) {
			logWarning("Cannot disconnect, Socket is null");
			return;
		}

		logInfo("+---+ Disconnecting...");

		try {
			socket.close();
		} catch (IOException e) {
			notifyError(e);
		} finally {
			socket = null;
		}
		setState(ConnectionState.Idle);
	}

	protected final void notifyError(Exception e) {
		logError("Error in Transceiver", e);
		for (TransceiverListener listener : listeners) {
			listener.onError(e);
		}
	}

	protected final void notifyNewPacket(Packet packet) {
		logDebug("New Packet received from Remote BT Device: " + name + "\n  Data: " + packet.toString());
		for (TransceiverListener listener : listeners) {
			listener.onIncomingPacket(packet);
		}
	}

	protected final void notifyStateChanged(ConnectionState state) {
		for (TransceiverListener listener : listeners) {
			listener.onStateChange(state);
		}
	}

	public boolean isConnected() {
		return state != ConnectionState.Idle;
	}
}
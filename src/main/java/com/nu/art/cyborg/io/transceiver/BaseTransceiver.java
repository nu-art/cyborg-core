package com.nu.art.cyborg.io.transceiver;

import android.os.Handler;

import com.nu.art.belog.Logger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.modules.ThreadsModule;

import java.io.IOException;
import java.net.SocketException;

import static com.nu.art.cyborg.io.transceiver.ConnectionState.Connected;
import static com.nu.art.cyborg.io.transceiver.ConnectionState.Connecting;
import static com.nu.art.cyborg.io.transceiver.ConnectionState.Idle;

public abstract class BaseTransceiver
		extends Logger {

	protected SocketWrapper socket;

	private ConnectionState state = Idle;

	protected final String name;

	protected final PacketSerializer packetSerializer;

	private final Handler receiver;

	private final Handler transmitter;

	private boolean listen = true;

	private TransceiverListener[] listeners = {};

	private Runnable connectAndListen = new Runnable() {
		@Override
		public void run() {
			try {
				listen = true;
				while (listen) {
					setState(Connecting);
					socket = connectImpl();
					setState(Connected);

					while (socket.isConnected()) {
						try {
							processPacket();
						} catch (SocketException e) {
							break;
						} catch (IOException e) {
							try {
								socket.close();
							} catch (IOException e1) {
								notifyError(e);
							}
						} catch (Exception e) {
							notifyError(e);
						}
					}
				}
				//TODO all logic here needs to be mapped to the disconnect method!!
			} catch (Exception e) {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e1) {
					notifyError(e);
				}

				notifyError(e);
			} finally {
				socket = null;
				setState(Idle);
			}
		}
	};

	public BaseTransceiver(String name, PacketSerializer packetSerializer) {
		this.name = name;
		setTag(name);
		this.packetSerializer = packetSerializer;
		transmitter = CyborgBuilder.getModule(ThreadsModule.class).getDefaultHandler("Transmitter - " + name);
		receiver = CyborgBuilder.getModule(ThreadsModule.class).getDefaultHandler("Receiver - " + name);
	}

	public final void setOneShot() {
		listen = false;
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
					packetSerializer.serializePacket(socket.getOutputStream(), packet);
				} catch (IOException e) {
					notifyError(e);
				}
			}
		});
	}

	public void connect() {
		logInfo("Connecting");

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

	public final void removeListener(TransceiverListener listener) {
		listeners = ArrayTools.removeElement(listeners, listener);
	}

	protected ConnectionState getState() {
		return state;
	}

	public synchronized boolean isState(ConnectionState state) {
		return this.state == state;
	}

	public final synchronized void setState(ConnectionState newState) {
		if (state == newState)
			return;

		logDebug("State changed: " + state + " => " + newState);
		this.state = newState;
		notifyStateChanged(newState);
	}

	protected abstract SocketWrapper connectImpl()
			throws Exception;

	public void disconnect() {
		logInfo("Disconnecting");

		if (state == Idle) {
			logWarning("Cannot disconnect, State is Idle");
			return;
		}

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
		}
		setState(Idle);
	}

	protected final void notifyError(Exception e) {
		logError("Error in Transceiver", e);
		for (TransceiverListener listener : listeners) {
			listener.onError(e);
		}
	}

	protected final void notifyNewPacket(Packet packet) {
		logDebug("New Packet received to: " + name + "\n  Data: " + packet.toString());
		for (TransceiverListener listener : listeners) {
			listener.onIncomingPacket(packet);
		}
	}

	protected final void notifyStateChanged(ConnectionState state) {
		for (TransceiverListener listener : listeners) {
			listener.onStateChange(state);
		}
	}
}
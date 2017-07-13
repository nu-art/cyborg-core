package com.nu.art.cyborg.io.transceiver.wifi;

import com.nu.art.cyborg.io.transceiver.PacketSerializer;
import com.nu.art.cyborg.io.transceiver.SocketWrapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class WifiServerTransceiver
		extends WifiTransceiver {

	private final int serverPort;

	private ServerSocket serverSocket;

	public WifiServerTransceiver(int serverPort, String name, PacketSerializer packetSerializer) {
		super(name, packetSerializer);
		this.serverPort = serverPort;
	}

	@Override
	protected SocketWrapper connectImpl()
			throws Exception {

		serverSocket = new ServerSocket(serverPort);
		Socket socket = serverSocket.accept();
		return new WifiSocketWrapper(socket);
	}

	public void disconnectImpl() {
		super.disconnect();
		try {
			serverSocket.close();
		} catch (IOException e) {
			notifyError(e);
		}
	}
}
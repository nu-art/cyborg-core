package com.nu.art.cyborg.io.transceiver.wifi;

import com.nu.art.cyborg.io.transceiver.PacketSerializer;
import com.nu.art.cyborg.io.transceiver.SocketWrapper;

import java.net.ServerSocket;
import java.net.Socket;

public final class WifiServerTransceiver
		extends WifiTransceiver {

	private final int serverPort;

	public WifiServerTransceiver(int serverPort, String name, PacketSerializer packetSerializer) {
		super(name, packetSerializer);
		this.serverPort = serverPort;
	}

	@Override
	protected SocketWrapper connectImpl()
			throws Exception {

		ServerSocket serverSocket = new ServerSocket(serverPort);
		Socket socket = serverSocket.accept();
		return new WifiSocketWrapper(socket);
	}
}
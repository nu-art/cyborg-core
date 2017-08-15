package com.nu.art.cyborg.io.transceiver.wifi;

import com.nu.art.cyborg.io.transceiver.PacketSerializer;
import com.nu.art.cyborg.io.transceiver.SocketWrapper;

import java.net.InetSocketAddress;
import java.net.Socket;

public class WifiClientTransceiver
		extends WifiTransceiver {

	private final String serverIpAddress;

	private final int serverPort;

	private int timeout = 30000;

	public WifiClientTransceiver(String serverIpAddress, int serverPort, String name, PacketSerializer packetSerializer) {
		super(name, packetSerializer);
		this.serverIpAddress = serverIpAddress;
		this.serverPort = serverPort;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	protected SocketWrapper connectImpl()
			throws Exception {
		setOneShot();
		logDebug("Connecting on: " + serverIpAddress + ":" + serverPort);
		Socket socket = new Socket();
		socket.bind(null);
		socket.connect((new InetSocketAddress(serverIpAddress, serverPort)), this.timeout);

		return new WifiSocketWrapper(socket);
	}

	@Override
	public String getRemoteAddress() {
		return serverIpAddress;
	}
}

/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	private String remoteAddress;

	public WifiServerTransceiver(int serverPort, String name, PacketSerializer packetSerializer) {
		super(name, packetSerializer);
		this.serverPort = serverPort;
	}

	@Override
	protected SocketWrapper connectImpl()
			throws Exception {

		serverSocket = new ServerSocket(serverPort);
		Socket socket = serverSocket.accept();
		remoteAddress = socket.getInetAddress().getHostAddress();
		return new WifiSocketWrapper(socket);
	}

	public void disconnectImpl() {
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			notifyError(e);
		}
	}

	@Override
	public String getRemoteAddress() {
		return remoteAddress;
	}
}
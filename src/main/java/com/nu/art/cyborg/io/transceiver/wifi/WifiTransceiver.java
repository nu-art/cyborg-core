package com.nu.art.cyborg.io.transceiver.wifi;

import com.nu.art.cyborg.io.transceiver.BaseTransceiver;
import com.nu.art.cyborg.io.transceiver.PacketSerializer;
import com.nu.art.cyborg.io.transceiver.SocketWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

abstract class WifiTransceiver
		extends BaseTransceiver {

	class WifiSocketWrapper
			implements SocketWrapper {

		private final Socket socket;

		public WifiSocketWrapper(Socket socket) {
			this.socket = socket;
		}

		@Override
		public OutputStream getOutputStream()
				throws IOException {
			return socket.getOutputStream();
		}

		@Override
		public InputStream getInputStream()
				throws IOException {
			return socket.getInputStream();
		}

		@Override
		public void close()
				throws IOException {
			socket.close();
		}

		@Override
		public boolean isConnected()
				throws IOException {
			return socket.isConnected();
		}
	}

	public WifiTransceiver(String name, PacketSerializer packetSerializer) {
		super(name, packetSerializer);
	}
}



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

import com.nu.art.cyborg.io.transceiver.BaseTransceiver;
import com.nu.art.cyborg.io.transceiver.PacketSerializer;
import com.nu.art.cyborg.io.transceiver.SocketWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class WifiTransceiver
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

	public abstract String getRemoteAddress();
}



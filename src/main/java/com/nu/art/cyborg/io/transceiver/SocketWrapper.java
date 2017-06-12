package com.nu.art.cyborg.io.transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SocketWrapper {

	OutputStream getOutputStream()
			throws IOException;

	InputStream getInputStream()
			throws IOException;

	void close()
			throws IOException;

	boolean isConnected()
			throws IOException;
}


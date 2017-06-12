package com.nu.art.cyborg.io.transceiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface PacketSerializer {

	void serializePacket(OutputStream os, Packet packet)
			throws IOException;

	Packet extractPacket(InputStream inputStream)
			throws IOException;
}

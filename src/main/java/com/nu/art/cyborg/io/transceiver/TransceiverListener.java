package com.nu.art.cyborg.io.transceiver;

public interface TransceiverListener {

	void onStateChange(ConnectionState newState);

	void onError(Exception e);

	void onIncomingPacket(Packet packet);
}

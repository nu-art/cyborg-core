package com.nu.art.cyborg.common.implementors;

import com.nu.art.cyborg.io.transceiver.ConnectionState;
import com.nu.art.cyborg.io.transceiver.Packet;
import com.nu.art.cyborg.io.transceiver.TransceiverListener;

/**
 * Created by tacb0ss on 05/07/2017.
 */

public class TransceiverListenerImpl
		implements TransceiverListener {

	@Override
	public void onStateChange(ConnectionState newState) {}

	@Override
	public void onError(Exception e) {}

	@Override
	public void onIncomingPacket(Packet packet) {}
}

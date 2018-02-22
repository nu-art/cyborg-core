package com.nu.art.cyborg.io.transceiver;

/**
 * Created by tacb0ss on 21/02/2018.
 */

public interface PacketSendingListener {
	void onSuccess();
	void onError(Throwable t);
}

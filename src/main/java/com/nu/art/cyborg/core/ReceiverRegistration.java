package com.nu.art.cyborg.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

/**
 * Flags for {@code Context.registerReceiver} on API 33+ / targetSdk 34+.
 * System broadcasts must use {@link #EXPORTED}; app-local broadcasts use {@link #NOT_EXPORTED}.
 */
public final class ReceiverRegistration {

	public static final int EXPORTED = Context.RECEIVER_EXPORTED;

	public static final int NOT_EXPORTED = Context.RECEIVER_NOT_EXPORTED;

	private ReceiverRegistration() {}

	public static int flags(boolean exported) {
		return exported ? EXPORTED : NOT_EXPORTED;
	}

	public static Intent peekStickyBroadcast(Context context, IntentFilter filter) {
		return ContextCompat.registerReceiver(context, null, filter, NOT_EXPORTED);
	}
}

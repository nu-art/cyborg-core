package com.nu.art.cyborg.core;

import android.content.Context;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Test_ReceiverRegistration {

	@Test
	public void flags_NotExported() {
		assertEquals(Context.RECEIVER_NOT_EXPORTED, ReceiverRegistration.flags(false));
	}

	@Test
	public void flags_Exported() {
		assertEquals(Context.RECEIVER_EXPORTED, ReceiverRegistration.flags(true));
	}

	@Test
	public void constantsMatchPlatform() {
		assertEquals(Context.RECEIVER_EXPORTED, ReceiverRegistration.EXPORTED);
		assertEquals(Context.RECEIVER_NOT_EXPORTED, ReceiverRegistration.NOT_EXPORTED);
	}
}

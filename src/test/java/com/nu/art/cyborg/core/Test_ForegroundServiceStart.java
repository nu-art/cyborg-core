package com.nu.art.cyborg.core;

import android.content.pm.ServiceInfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Test_ForegroundServiceStart {

	@Test
	public void manifestType_MatchesPlatform() {
		assertEquals(ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST, ForegroundServiceStart.manifestType());
	}
}

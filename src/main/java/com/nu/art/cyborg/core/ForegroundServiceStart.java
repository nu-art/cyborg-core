package com.nu.art.cyborg.core;

import android.app.Notification;
import android.app.Service;

import androidx.core.app.ServiceCompat;

import android.content.pm.ServiceInfo;

/**
 * Starts a foreground service using the type declared on the {@code <service>} in the manifest.
 */
public final class ForegroundServiceStart {

	private ForegroundServiceStart() {}

	public static int manifestType() {
		return ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST;
	}

	public static void start(Service service, int id, Notification notification) {
		start(service, id, notification, manifestType());
	}

	public static void start(Service service, int id, Notification notification, int foregroundServiceType) {
		ServiceCompat.startForeground(service, id, notification, foregroundServiceType);
	}
}

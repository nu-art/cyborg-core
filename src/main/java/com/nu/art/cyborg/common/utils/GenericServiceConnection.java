package com.nu.art.cyborg.common.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nu.art.belog.Logger;
import com.nu.art.cyborg.core.CyborgServiceBase.BaseBinder;

public final class GenericServiceConnection<_ServiceType extends Service>
		extends Logger
		implements ServiceConnection {

	public interface ServiceConnectionListener<_ServiceType extends Service> {

		void onServiceConnected(_ServiceType serviceType);

		void onServiceDisconnected();
	}

	private final ServiceConnectionListener<_ServiceType> listener;

	private final Class<_ServiceType> serviceType;

	private _ServiceType service;

	public GenericServiceConnection(Class<_ServiceType> serviceType, ServiceConnectionListener<_ServiceType> listener) {
		this.serviceType = serviceType;
		this.listener = listener;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onServiceConnected(ComponentName className, IBinder binder) {
		service = ((BaseBinder<_ServiceType>) binder).getService();
		logDebug("Service connected, " + serviceType + ": " + service.toString()
																																 .split("@")[1]);

		listener.onServiceConnected(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName className) {
		logDebug("Service disconnected, " + serviceType + ": " + service.toString()
																																		.split("@")[1]);
		listener.onServiceDisconnected();
	}
}

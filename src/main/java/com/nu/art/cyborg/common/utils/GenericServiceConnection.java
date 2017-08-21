/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nu.art.cyborg.common.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nu.art.belog.Logger;
import com.nu.art.cyborg.core.CyborgServiceBase.BaseBinder;

import java.util.ArrayList;

public final class GenericServiceConnection<_ServiceType extends Service>
		extends Logger
		implements ServiceConnection {

	public interface ServiceConnectionListener<_ServiceType extends Service> {

		void onServiceConnected(_ServiceType service);

		void onServiceDisconnected(_ServiceType service);
	}

	public static abstract class ServiceConnectionListenerImpl<_ServiceType extends Service>
			implements ServiceConnectionListener<_ServiceType> {

		@Override
		public void onServiceConnected(_ServiceType service) {}

		@Override
		public void onServiceDisconnected(_ServiceType service) {}
	}

	private ArrayList<ServiceConnectionListener<_ServiceType>> listeners = new ArrayList<>();

	private final Class<_ServiceType> serviceType;

	private _ServiceType service;

	public GenericServiceConnection(Class<_ServiceType> serviceType) {
		this.serviceType = serviceType;
	}

	public void addListener(ServiceConnectionListener<_ServiceType> listener) {
		this.listeners.add(listener);
		if (service == null)
			return;

		listener.onServiceConnected(service);
	}

	public void removeListener(ServiceConnectionListener<_ServiceType> listener) {
		this.listeners.remove(listener);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onServiceConnected(ComponentName className, IBinder binder) {
		service = ((BaseBinder<_ServiceType>) binder).getService();
		logDebug("Service connected, " + serviceType + ": " + service.toString().split("@")[1]);
		for (ServiceConnectionListener<_ServiceType> listener : listeners) {
			listener.onServiceConnected(service);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName className) {
		logDebug("Service disconnected, " + serviceType + ": " + service.toString().split("@")[1]);
		for (ServiceConnectionListener<_ServiceType> listener : listeners) {
			listener.onServiceDisconnected(service);
		}
		service = null;
	}
}

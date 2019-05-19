/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
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

package com.nu.art.cyborg.modules;

import android.Manifest.permission;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.cyborg.core.modules.ThreadsModule;

import java.net.InetSocketAddress;
import java.net.Socket;

@ModuleDescriptor(usesPermissions = permission.INTERNET)
public class InternetConnectivityModule
	extends CyborgModule {

	private int timeout = 5000;

	public interface InternetConnectivityListener {

		void onInternetConnectivityChanged();
	}

	private Handler handler;

	private volatile Boolean isConnected;

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	protected void init() {
		handler = getModule(ThreadsModule.class).getDefaultHandler("Internet Check");
		registerReceiver(ConnectivityCheckReceiver.class);
		postOnUI(new Runnable() {
			@Override
			public void run() {
				checkInternetConnectionAsync();
			}
		});
	}

	public synchronized boolean isConnected() {
		return isConnected != null && isConnected;
	}

	public synchronized void setConnected(boolean connected) {
		isConnected = connected;
	}

	private void checkInternetConnectionAsync() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				boolean connected;
				try {
					logVerbose("checking connectivity");
					Socket sock = new Socket();
					sock.connect(new InetSocketAddress("8.8.8.8", 53), timeout);
					sock.close();
					connected = true;
				} catch (Exception e) {
					logError("Couldn't ping 8.8.8.8", e);
					connected = false;
				}

				if (isConnected != null && isConnected == connected)
					return;

				setConnected(connected);
				dispatchGlobalEvent("Internet Check - " + (isConnected ? "Has Internet"
				                                                       : "No Internet"), InternetConnectivityListener.class, new Processor<InternetConnectivityListener>() {
					@Override
					public void process(InternetConnectivityListener listener) {
						listener.onInternetConnectivityChanged();
					}
				});
			}
		});
	}

	private static class ConnectivityCheckReceiver
		extends CyborgReceiver<InternetConnectivityModule> {

		protected ConnectivityCheckReceiver() {
			super(InternetConnectivityModule.class, ConnectivityManager.CONNECTIVITY_ACTION);
		}

		@Override
		protected void onReceive(Intent intent, InternetConnectivityModule module) {
			module.checkInternetConnectionAsync();
		}
	}
}
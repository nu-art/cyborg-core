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

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.cyborg.core.modules.ThreadsModule;

import java.net.InetAddress;

public class InternetConnectivityModule
	extends CyborgModule {

	private static final String DEFAULT_HOST = "google.com";

	public interface InternetConnectivityListener {

		void onInternetConnectivityChanged();
	}

	private Handler handler;

	private volatile boolean isConnected;

	@Override
	protected void init() {
		handler = getModule(ThreadsModule.class).getDefaultHandler("Internet Check");
		registerReceiver(ConnectivityCheckReceiver.class);
	}

	public boolean isConnected() {
		return isConnected;
	}

	private void checkInternetConnectionAsync() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					InetAddress.getByName(DEFAULT_HOST);
					isConnected = true;
				} catch (Exception e) {
					logError("Couldn't ping google.com", e);
					isConnected = false;
				}
				dispatchGlobalEvent("Internet Check - " + (isConnected ? "Has Internet" : "No Internet"), new Processor<InternetConnectivityListener>() {
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
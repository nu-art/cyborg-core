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

package com.nu.art.cyborg.modules.wifi;

import android.content.Intent;
import android.net.wifi.WifiManager;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.Condition;
import com.nu.art.cyborg.core.CyborgModuleItem;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.reflection.tools.ReflectiveTools;

/**
 * Created by tacb0ss on 12/07/2017.
 */

@SuppressWarnings("MissingPermission")
public class WifiItem_AdapterState
	extends CyborgModuleItem {

	public interface WifiAdapterStateListener {

		void onWifiAdapterStateChanged(WifiAdapterState state);
	}

	public enum WifiAdapterState {
		Disabled(WifiManager.WIFI_STATE_DISABLED),
		Enabling(WifiManager.WIFI_STATE_ENABLING),
		Enabled(WifiManager.WIFI_STATE_ENABLED),
		Disabling(WifiManager.WIFI_STATE_DISABLING),;

		private final int wifiState;

		WifiAdapterState(int wifiState) {
			this.wifiState = wifiState;
		}

		static WifiAdapterState getState(final int wifiState) {
			return ReflectiveTools.findMatchingEnumItem(WifiAdapterState.class, new Condition<WifiAdapterState>() {
				@Override
				public boolean checkCondition(WifiAdapterState wifiAdapterState) {
					return wifiAdapterState.wifiState == wifiState;
				}
			});
		}
	}

	private WifiManager wifiManager;

	private WifiAdapterStateMonitor monitor;

	@Override
	protected void init() {
		monitor = new WifiAdapterStateMonitor();

		wifiManager = getSystemService(WifiService);
	}

	void onWifiAdapterStateChanged() {
		monitor.onWifiAdapterStateChanged();
	}

	public void enableAdapter() {
		logDebug("Enable WIFI Adapter");
		wifiManager.setWifiEnabled(true);
	}

	public boolean isAdapterEnabled() {
		return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}

	public void disableAdapter() {
		logDebug("Disable WIFI Adapter");
		wifiManager.setWifiEnabled(false);
	}

	void enable(boolean enable) {
		if (enable)
			cyborg.registerReceiver(WifiAdapterStateReceiver.class, WifiManager.WIFI_STATE_CHANGED_ACTION);
		else
			cyborg.unregisterReceiver(WifiAdapterStateReceiver.class);
	}

	public static class WifiAdapterStateReceiver
		extends CyborgReceiver<WifiModule> {

		protected WifiAdapterStateReceiver() {
			super(WifiModule.class);
		}

		@Override
		protected void onReceive(Intent intent, WifiModule module) {
			switch (intent.getAction()) {
				case WifiManager.WIFI_STATE_CHANGED_ACTION:
					module.onWifiAdapterStateChanged();
					break;
			}
		}
	}

	private class WifiAdapterStateMonitor {

		private WifiAdapterState state;

		private void onWifiAdapterStateChanged() {
			WifiAdapterState state = WifiAdapterState.getState(wifiManager.getWifiState());
			if (state == null)
				return;

			if (this.state == state)
				return;

			setState(state);
		}

		private void setState(final WifiAdapterState state) {
			this.state = state;

			dispatchGlobalEvent("Wifi adapter state changed: " + state, WifiAdapterStateListener.class, new Processor<WifiAdapterStateListener>() {
				@Override
				public void process(WifiAdapterStateListener listener) {
					listener.onWifiAdapterStateChanged(state);
				}
			});
		}
	}
}

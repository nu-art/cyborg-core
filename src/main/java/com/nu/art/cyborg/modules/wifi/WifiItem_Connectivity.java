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

package com.nu.art.cyborg.modules.wifi;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModuleItem;
import com.nu.art.cyborg.core.CyborgReceiver;

import java.util.List;

/**
 * Created by tacb0ss on 12/07/2017.
 */

@SuppressWarnings("MissingPermission")
public class WifiItem_Connectivity
		extends CyborgModuleItem {

	public interface WifiConnectivityListener {

		void onWifiConnectivityStateChanged();

		void onWifiConnectionError();
	}

	public enum WifiConnectivityState {
		Disconnected,
		Connected,
	}

	private WifiManager wifiManager;

	private ConnectivityManager connectivityManager;

	private WifiConnectivityMonitor monitor;

	private long connectivityTimeout = 20000;

	@Override
	protected void init() {
		monitor = new WifiConnectivityMonitor();

		wifiManager = getSystemService(WifiService);
		connectivityManager = getSystemService(ConnectivityService);
		monitor.init();
	}

	final void setConnectivityTimeout(long connectivityTimeout) {
		this.connectivityTimeout = connectivityTimeout;
	}

	final boolean isConnectedToWifi() {
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo == null || !wifiNetworkInfo.isConnected())
			return false;

		return true;
	}

	final void removeWifi(String name) {
		name = "\"" + name + "\"";
		List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
		if (wifiConfigurations == null)
			return;

		for (WifiConfiguration wifiConfig : wifiConfigurations) {
			String networkName = wifiConfig.SSID;
			if (!networkName.equals(name))
				continue;

			logDebug("removing network: " + networkName);
			wifiManager.removeNetwork(wifiConfig.networkId);
		}
		wifiManager.saveConfiguration();
	}

	final boolean hasWifiConfiguration(String name) {
		name = "\"" + name + "\"";
		List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
		if (wifiConfigurations == null)
			return false;

		for (WifiConfiguration wifiConfig : wifiConfigurations) {
			String networkName = wifiConfig.SSID;
			if (!networkName.equals(name))
				continue;

			return true;
		}

		return false;
	}

	final void deleteAllWifiConfigurations() {
		List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
		if (wifiConfigurations == null)
			return;

		for (WifiConfiguration wifiConfig : wifiConfigurations) {
			String networkName = wifiConfig.SSID;
			if (wifiManager.removeNetwork(wifiConfig.networkId))
				logDebug("Successfully removed network: " + networkName);
			else
				logDebug("Error removing network: " + networkName);
		}
		wifiManager.saveConfiguration();
	}

	void disconnectFromWifi() {
		wifiManager.disconnect();
	}

	final void connectToWifi(String wifiName, String password) {
		if (wifiName == null)
			throw new BadImplementationException("MUST provide wifiName");

		removeWifi(wifiName);

		final WifiConfiguration wifiConfig = new WifiConfiguration();
		wifiConfig.SSID = "\"" + wifiName + "\"";
		wifiConfig.preSharedKey = "\"" + password + "\"";

		int netId = wifiManager.addNetwork(wifiConfig);
		if (netId == -1) {
			dispatchGlobalEvent("Error while connecting to WiFi: " + wifiName, WifiConnectivityListener.class, new Processor<WifiConnectivityListener>() {
				@Override
				public void process(WifiConnectivityListener listener) {
					listener.onWifiConnectionError();
				}
			});
			return;
		}

		wifiManager.disconnect();
		wifiManager.enableNetwork(netId, true);
		wifiManager.reconnect();
		logInfo("Connecting to Wifi: " + wifiName);
	}

	final String getMyIpAddress() {
		if (!isConnectedToWifi()) {
			throw new BadImplementationException("Not connected wifi!!");
		}

		WifiInfo connectionInfo = wifiManager.getConnectionInfo();
		return Formatter.formatIpAddress(connectionInfo.getIpAddress());
	}

	final String getConnectedWifiName() {
		String wifiName = wifiManager.getConnectionInfo().getSSID();
		return wifiName.substring(1, wifiName.length() - 1);
	}

	public boolean isConnectivityState(WifiConnectivityState connectivityState) {
		return monitor.isState(connectivityState);
	}

	void onConnectionChanged() {
		monitor.onConnectionChanged();
	}

	void enable(boolean enable) {
		if (enable)
			cyborg.registerReceiver(WifiConnectivityReceiver.class, WifiManager.NETWORK_STATE_CHANGED_ACTION);
		else
			cyborg.unregisterReceiver(WifiConnectivityReceiver.class);
	}

	private class WifiConnectivityMonitor
			implements Runnable {

		WifiConnectivityState state;

		long started;

		private void init() {
			state = isConnectedToWifi() ? WifiConnectivityState.Connected : WifiConnectivityState.Disconnected;
		}

		void onConnectionChanged() {
			started = System.currentTimeMillis();
			removeAndPostOnUI(200, this);
		}

		@Override
		public void run() {
			WifiConnectivityState newState = isConnectedToWifi() ? WifiConnectivityState.Connected : WifiConnectivityState.Disconnected;

			if (newState == state) {
				if (System.currentTimeMillis() - started > connectivityTimeout) {
					return;
				}

				removeAndPostOnUI(200, this);
				return;
			}

			state = newState;
			dispatchGlobalEvent("Wifi connectivity state: " + state, WifiConnectivityListener.class, new Processor<WifiConnectivityListener>() {
				@Override
				public void process(WifiConnectivityListener listener) {
					listener.onWifiConnectivityStateChanged();
				}
			});
		}

		private boolean isState(WifiConnectivityState state) {
			return this.state == state;
		}
	}

	public static class WifiConnectivityReceiver
			extends CyborgReceiver<WifiModule> {

		protected WifiConnectivityReceiver() {
			super(WifiModule.class);
		}

		@Override
		protected void onReceive(Intent intent, WifiModule module) {
			switch (intent.getAction()) {
				case WifiManager.NETWORK_STATE_CHANGED_ACTION:
					module.onConnectionChanged();
					break;
			}
		}
	}
}

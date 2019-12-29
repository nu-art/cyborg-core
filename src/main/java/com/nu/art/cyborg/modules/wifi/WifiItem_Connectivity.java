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

import android.Manifest.permission;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiConfiguration.Status;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModuleItem;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.cyborg.modules.wifi.WifiItem_Scanner.WifiSecurityMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nu.art.cyborg.modules.wifi.WifiItem_Scanner.WifiSecurityMode.EAP;

/**
 * Created by TacB0sS on 12/07/2017.
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

	private Set<String> keys = new HashSet<>();
	private long connectivityTimeout = 20000;
	private boolean enabled;

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
		NetworkInfo wifiNetworkInfo;
		try {
			wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		} catch (Exception e) {
			logWarning("Error while getting wifi network info", e);
			return false;
		}

		if (wifiNetworkInfo == null || !wifiNetworkInfo.isConnected())
			return false;

		try {
			return checkIfReallyConnected(wifiManager.getConnectionInfo());
		} catch (SecurityException e) {
			throw ExceptionGenerator.missingPermissionsToPerformAction("Check wifi connectivity", permission.ACCESS_WIFI_STATE, e);
		}
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

	final void connectToWifi(String wifiName, String password, WifiSecurityMode securityMode) {
		if (wifiName == null)
			throw new BadImplementationException("MUST provide wifiName");
		logDebug("##### connectToWifi called with wifiName:"+wifiName);
		List<ScanResult> scanResults = wifiManager.getScanResults();
		int scanResultLen = scanResults != null ? scanResults.size(): 0;

		String bssid = null;
		logDebug("##### connectToWifi scanResults:"+scanResults+", num of results:"+scanResultLen);
		for (ScanResult result: scanResults) {
			logDebug("##### connectToWifi res SSID:"+result.SSID+", BSSID:"+result.BSSID+", frequency: "+result.frequency);
			if (result.SSID.equals(wifiName)) {
				bssid = result.BSSID;
				logDebug("##### connectToWifi FOUND BSSID:"+bssid);
			}
		}

		int netId = getNetId(wifiName, bssid);
		boolean saved = true;
		if (netId == -1) {
			removeWifi(wifiName);

			final WifiConfiguration wifiConfig = createWifiConfiguration(wifiName, bssid, password, securityMode);
			netId = wifiManager.addNetwork(wifiConfig);
			saved = wifiManager.saveConfiguration();
		}

		if (netId == -1 || !saved) {
			dispatchGlobalEvent("Error while connecting to WiFi: " + wifiName, WifiConnectivityListener.class, new Processor<WifiConnectivityListener>() {
				@Override
				public void process(WifiConnectivityListener listener) {
					listener.onWifiConnectionError();
				}
			});
			return;
		}

		disconnectFromWifi();
		wifiManager.enableNetwork(netId, true);
		wifiManager.reconnect();
		logInfo("Connecting to Wifi: " + wifiName+", bssid "+bssid);
	}

	private int getNetId(String wifiName, String bssid) {
		int netId = -1;
		List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
		if (configuredNetworks != null) {
			for (WifiConfiguration configuration : configuredNetworks) {
				if (bssid != null) {
					if (bssid.equals(configuration.BSSID)) {
						logDebug("##### connectToWifi found configur Fation for access point with bssid: " + bssid);
						netId = configuration.networkId;
					}
				} else if (configuration.SSID != null && configuration.SSID.equals("\"" + wifiName + "\"")) {
					logDebug("##### connectToWifi found configuration for wifi name = " + wifiName);
					netId = configuration.networkId;
				}
			}
		}
		logDebug("##### connectToWifi configuredNetworks "+configuredNetworks+", bssid: "+bssid+", found netID = "+netId);
		return netId;
	}

	private WifiConfiguration createWifiConfiguration(String wifiName, String bssid, String password, WifiSecurityMode securityMode) {
		WifiConfiguration wifiConfiguration = new WifiConfiguration();

		wifiConfiguration.SSID = "\"" + wifiName + "\"";
		wifiConfiguration.BSSID = bssid;
		switch (securityMode) {
			case WEP:
				wifiConfiguration.allowedKeyManagement.set(KeyMgmt.NONE);
				wifiConfiguration.allowedProtocols.set(Protocol.RSN);
				wifiConfiguration.allowedProtocols.set(Protocol.WPA);
				wifiConfiguration.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				wifiConfiguration.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				wifiConfiguration.allowedPairwiseCiphers.set(PairwiseCipher.CCMP);
				wifiConfiguration.allowedPairwiseCiphers.set(PairwiseCipher.TKIP);
				wifiConfiguration.allowedGroupCiphers.set(GroupCipher.WEP40);
				wifiConfiguration.allowedGroupCiphers.set(GroupCipher.WEP104);

				if (password.matches("^[0-9a-fA-F]+$")) {
					wifiConfiguration.wepKeys[0] = password;
				} else {
					wifiConfiguration.wepKeys[0] = "\"".concat(password).concat("\"");
				}
				wifiConfiguration.wepTxKeyIndex = 0;
				break;

			case EAP:
			case PSK:
				wifiConfiguration.preSharedKey = "\"" + password + "\"";
				wifiConfiguration.hiddenSSID = true;
				wifiConfiguration.status = Status.ENABLED;
				wifiConfiguration.allowedGroupCiphers.set(GroupCipher.TKIP);
				wifiConfiguration.allowedGroupCiphers.set(GroupCipher.CCMP);
				wifiConfiguration.allowedKeyManagement.set(securityMode == EAP ? KeyMgmt.WPA_EAP : KeyMgmt.WPA_PSK);
				wifiConfiguration.allowedPairwiseCiphers.set(PairwiseCipher.TKIP);
				wifiConfiguration.allowedPairwiseCiphers.set(PairwiseCipher.CCMP);
				wifiConfiguration.allowedProtocols.set(Protocol.RSN);
				wifiConfiguration.allowedProtocols.set(Protocol.WPA);
				break;

			case OPEN:
				wifiConfiguration.allowedKeyManagement.set(KeyMgmt.NONE);
				break;

			default:
				wifiConfiguration.preSharedKey = "\"" + password + "\"";
				break;
		}

		return wifiConfiguration;
	}

	final WifiInfo getWifiConnectionInfo() {
		if (!isConnectedToWifi()) {
			throw new BadImplementationException("Not connected wifi!!");
		}

		return wifiManager.getConnectionInfo();
	}

	final String getMyIpAddress() {
		WifiInfo connectionInfo = getWifiConnectionInfo();
		return Formatter.formatIpAddress(connectionInfo.getIpAddress());
	}

	final String getConnectedWifiName() {
		WifiInfo connectionInfo = wifiManager.getConnectionInfo();

		if (!checkIfReallyConnected(connectionInfo))
			return null;

		String wifiName = connectionInfo.getSSID();
		if (wifiName == null)
			return null;

		return wifiName.substring(1, wifiName.length() - 1);
	}

	private boolean checkIfReallyConnected(WifiInfo connectionInfo) {
		if (connectionInfo == null)
			return false;

		if ("00:00:00:00:00:00".equals(connectionInfo.getBSSID())) {
			logWarning("Android bug, not connect yet think that it does");
			return false;
		}

		if (connectionInfo.getNetworkId() == -1) {
			logWarning("Android bug, not connect yet think that it does");
			return false;
		}
		return true;
	}

	public boolean isConnectivityState(WifiConnectivityState connectivityState) {
		return monitor.isState(connectivityState);
	}

	void onConnectionChanged() {
		monitor.onConnectionChanged();
	}

	void enable(boolean enable) {
		if (enable && !this.enabled) {
			cyborg.registerReceiver(WifiConnectivityReceiver.class, WifiManager.NETWORK_STATE_CHANGED_ACTION);
		}

		if (!enable && this.enabled) {
			cyborg.unregisterReceiver(WifiConnectivityReceiver.class);
		}

		if (enable)
			monitor.init();
		this.enabled = enable;
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
			String action = intent.getAction();
			if (action == null)
				return;

			switch (action) {
				case WifiManager.NETWORK_STATE_CHANGED_ACTION:
					module.onConnectionChanged();
					break;
			}
		}
	}
}

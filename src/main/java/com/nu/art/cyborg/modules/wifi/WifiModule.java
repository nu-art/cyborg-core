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

import android.annotation.SuppressLint;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.net.wifi.WifiInfo;
import android.os.Build;

import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.modules.wifi.WifiItem_Connectivity.WifiConnectivityState;
import com.nu.art.cyborg.modules.wifi.WifiItem_Scanner.ScannedWifiInfo;
import com.nu.art.cyborg.modules.wifi.WifiItem_Scanner.WifiSecurityMode;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by TacB0sS on 04-Jul 2017.
 */

public class WifiModule
		extends CyborgModule {

	private WifiItem_Connectivity WifiConnectivity;

	private WifiItem_AdapterState WifiAdapter;

	private WifiItem_Scanner WifiNetworkScanner;

	@Override
	protected void init() {
		WifiConnectivity = createModuleItem(WifiItem_Connectivity.class);
		WifiAdapter = createModuleItem(WifiItem_AdapterState.class);
		WifiNetworkScanner = createModuleItem(WifiItem_Scanner.class);
	}

	final void onScanCompleted() {
		WifiNetworkScanner.onScanCompleted();
	}

	final void onConnectionChanged() {
		WifiConnectivity.onConnectionChanged();
	}

	final void onWifiAdapterStateChanged() {
		WifiAdapter.onWifiAdapterStateChanged();
	}

	public void listenForWifiNetworks(boolean enable) {
		WifiNetworkScanner.enable(enable);
	}

	public void listenForWifiConnectivity(boolean enable) {
		WifiConnectivity.enable(enable);
	}

	public void listenForWifiAdapterState(boolean enable) {
		WifiAdapter.enable(enable);
	}

	public void disableAdapter() {
		WifiAdapter.disableAdapter();
	}

	public void enableAdapter() {
		WifiAdapter.enableAdapter();
	}

	public ScannedWifiInfo[] getScanResults() {
		return WifiNetworkScanner.getScanResults();
	}

	public boolean isConnectedToWifi() {
		return WifiConnectivity.isConnectedToWifi();
	}

	public void connectToWifi(String wifiName, String password, WifiSecurityMode securityMode) {
		WifiConnectivity.connectToWifi(wifiName, password, securityMode);
	}

	public void disconnectFromWifi() {
		WifiConnectivity.disconnectFromWifi();
	}

	public void removeWifi(String connectedWifiName) {
		WifiConnectivity.removeWifi(connectedWifiName);
	}

	public boolean isAdapterEnabled() {
		return WifiAdapter.isAdapterEnabled();
	}

	public String getConnectedWifiName() {
		return WifiConnectivity.getConnectedWifiName();
	}

	public String getWifiIpAddress() {
		return WifiConnectivity.getMyIpAddress();
	}

	public boolean hasWifiConfiguration(String wifiName) {
		return WifiConnectivity.hasWifiConfiguration(wifiName);
	}

	public boolean isConnectivityState(WifiConnectivityState state) {
		return WifiConnectivity.isConnectivityState(state);
	}

	public void scanForWifiNetworks() {
		WifiNetworkScanner.startScan();
	}

	final void setConnectivityTimeout(long connectivityTimeout) {
		WifiConnectivity.setConnectivityTimeout(connectivityTimeout);
	}

	public void deleteAllWifiConfigurations() {
		WifiConnectivity.deleteAllWifiConfigurations();
	}

	public boolean hasAccessPoint(String wifiName) {
		return WifiNetworkScanner.hasAccessPoint(wifiName);
	}

	@SuppressLint( {
										 "HardwareIds",
										 "MissingPermission"
								 })
	public  String calculateMacAddress() {
		String macAddress = null;
		if (Build.VERSION.SDK_INT < 23) {
			// generate a unique id from MAC address of the WiFi
			WifiInfo info = getSystemService(WifiService).getConnectionInfo();
			return info.getMacAddress();
		}
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (!nif.getName().equalsIgnoreCase("wlan0"))
					continue;

				byte[] macBytes = nif.getHardwareAddress();
				if (macBytes == null) {
					macAddress = "";
					continue;
				}

				StringBuilder res1 = new StringBuilder();

				for (byte b : macBytes) {
					if (b == 0x00)
						res1.append("00" + ":");
					else
						res1.append(String.format("%02x:", b));
				}

				if (res1.length() > 0) {
					res1.deleteCharAt(res1.length() - 1);
				}

				macAddress = res1.toString();
				return macAddress;
			}
		} catch (Exception e) {
			// TODO What ddo weo we do once unable to get wifi mac
			logError("Error calculating the Wifi mac address exception", e);
		}

		//"Android For Work" code, added in version 24, needs more research.
		if (Build.VERSION.SDK_INT > 23) {
			DeviceAdminReceiver admin = new DeviceAdminReceiver();
			DevicePolicyManager devicepolicymanager = admin.getManager(getApplicationContext());
			ComponentName name1 = admin.getWho(getApplicationContext());
			if (devicepolicymanager.isAdminActive(name1)) {
				macAddress = devicepolicymanager.getWifiMacAddress(name1);
			}
		}

		return macAddress;
	}
}
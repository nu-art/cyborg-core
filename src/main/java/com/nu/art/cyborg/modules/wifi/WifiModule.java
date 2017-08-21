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

import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.modules.wifi.WifiItem_Connectivity.WifiConnectivityState;
import com.nu.art.cyborg.modules.wifi.WifiItem_Scanner.ScannedWifiInfo;

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

	public void connectToWifi(String wifiName, String password) {
		WifiConnectivity.connectToWifi(wifiName, password);
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
}

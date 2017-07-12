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

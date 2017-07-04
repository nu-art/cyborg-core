package com.nu.art.cyborg.modules;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.core.CyborgModule;

/**
 * Created by TacB0sS on 04-Jul 2017.
 */

public class CyborgWifiModule
		extends CyborgModule {

	private WifiManager wifiManager;

	@Override
	protected void init() {
		wifiManager = getSystemService(WifiService);
	}

	@SuppressWarnings("MissingPermission")
	public String getMyIpAddress() {
		if (!isConnectedToWifi()) {
			throw new BadImplementationException("Not connected wifi!!");
		}

		WifiInfo connectionInfo = wifiManager.getConnectionInfo();
		return Formatter.formatIpAddress(connectionInfo.getIpAddress());
	}

	public boolean isConnectedToWifi() {
		ConnectivityManager connectivityManager = getSystemService(ConnectivityService);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo == null || !wifiNetworkInfo.isConnected())
			return false;

		return true;
	}
}

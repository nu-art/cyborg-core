package com.nu.art.cyborg.modules;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgReceiver;

/**
 * Created by TacB0sS on 04-Jul 2017.
 */

public class CyborgWifiModule
		extends CyborgModule {

	public interface OnWifiConnectedListener {

		void onWifiConnected();
	}

	private WifiManager wifiManager;

	private final WifiConnectivityChecker WifiConnectivityChecker = new WifiConnectivityChecker();

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

	private void onConnectionChanged() {
		WifiConnectivityChecker.onConnectionChanged();
	}

	private void onScanCompleted() {

	}

	public void listenForWifiConnectivity() {
		registerReceiver(WifiNetworksReceiver.class, WifiManager.NETWORK_STATE_CHANGED_ACTION);
	}

	public static class WifiNetworksReceiver
			extends CyborgReceiver<CyborgWifiModule> {

		protected WifiNetworksReceiver() {
			super(CyborgWifiModule.class);
		}

		@Override
		protected void onReceive(Intent intent, CyborgWifiModule module) {
			switch (intent.getAction()) {
				case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
					module.onScanCompleted();
					break;

				case WifiManager.NETWORK_STATE_CHANGED_ACTION:
					module.onConnectionChanged();
					break;
			}
		}
	}

	private class WifiConnectivityChecker
			implements Runnable {

		long started;

		private boolean isConnectedToWifi;

		void onConnectionChanged() {
			started = System.currentTimeMillis();
			removeAndPostOnUI(200, this);
		}

		@Override
		public void run() {
			logVerbose("On network connection changed");
			if (!isConnectedToWifi()) {
				isConnectedToWifi = false;
				if (System.currentTimeMillis() - started < 2000)
					postOnUI(200, this);
				return;
			}

			if (isConnectedToWifi) {
				logDebug("Wifi already connected");
				return;
			}

			isConnectedToWifi = true;
			dispatchGlobalEvent("Wifi Connected", OnWifiConnectedListener.class, new Processor<OnWifiConnectedListener>() {
				@Override
				public void process(OnWifiConnectedListener listener) {
					listener.onWifiConnected();
				}
			});
		}
	}
}

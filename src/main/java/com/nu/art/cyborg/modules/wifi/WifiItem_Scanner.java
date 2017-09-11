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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.CyborgModuleItem;
import com.nu.art.cyborg.core.CyborgReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tacb0ss on 12/07/2017.
 */

@SuppressWarnings("MissingPermission")
public class WifiItem_Scanner
		extends CyborgModuleItem {

	public interface OnWifiUIListener {

		void onScanCompleted();
	}

	public enum WifiStrength {
		VeryWeak,
		Weak,
		Medium,
		Strong,
		VeryStrong,;
	}

	public static class ScannedWifiInfo {

		public WifiStrength strength = WifiStrength.VeryWeak;

		public ScanResult scanResult;

		public final String getName() {
			return scanResult.SSID;
		}
	}

	private ArrayList<ScannedWifiInfo> scanResults = new ArrayList<>();

	private WifiManager wifiManager;

	@Override
	protected void init() {
		wifiManager = getSystemService(WifiService);
	}

	void startScan() {
		wifiManager.startScan();
	}

	void onScanCompleted() {
		logInfo("On wifi scan completed");

		List<ScanResult> results = wifiManager.getScanResults();
		HashMap<String, ScannedWifiInfo> scannedWifis = new HashMap<>();

		WifiStrength[] values = WifiStrength.values();
		for (ScanResult result : results) {
			ScannedWifiInfo scannedWifi = scannedWifis.get(result.SSID);
			if (result.SSID.trim().length() == 0)
				continue;

			if (scannedWifi == null) {
				logVerbose("Found Wifi: " + result.SSID);
				scannedWifis.put(result.SSID, scannedWifi = new ScannedWifiInfo());
			}

			WifiStrength value = values[WifiManager.calculateSignalLevel(result.level, values.length)];
			if (scannedWifi.strength.ordinal() > value.ordinal())
				continue;

			scannedWifi.strength = value;
			scannedWifi.scanResult = result;
		}

		scanResults.clear();
		scanResults.addAll(scannedWifis.values());
		Collections.sort(scanResults, new Comparator<ScannedWifiInfo>() {
			@Override
			public int compare(ScannedWifiInfo o1, ScannedWifiInfo o2) {
				return o1.strength.ordinal() < o2.strength.ordinal() ? 1 : -1;
			}
		});

		dispatchEvent("Wifi Scan Completed", OnWifiUIListener.class, new Processor<OnWifiUIListener>() {
			@Override
			public void process(OnWifiUIListener listener) {
				listener.onScanCompleted();
			}
		});
	}

	public ScannedWifiInfo[] getScanResults() {
		return ArrayTools.asArray(scanResults, ScannedWifiInfo.class);
	}

	void enable(boolean enable) {
		if (enable)
			cyborg.registerReceiver(WifiNetworksReceiver.class, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		else
			cyborg.unregisterReceiver(WifiNetworksReceiver.class);
	}

	public static class WifiNetworksReceiver
			extends CyborgReceiver<WifiModule> {

		protected WifiNetworksReceiver() {
			super(WifiModule.class);
		}

		@Override
		protected void onReceive(Intent intent, WifiModule module) {
			switch (intent.getAction()) {
				case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
					module.onScanCompleted();
					break;
			}
		}
	}
}

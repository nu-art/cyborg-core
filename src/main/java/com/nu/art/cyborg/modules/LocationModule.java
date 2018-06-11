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

import android.Manifest.permission;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

import java.util.List;

import static com.nu.art.cyborg.modules.LocationModule.LocationService.GPS;
import static com.nu.art.cyborg.modules.LocationModule.LocationService.NETWORK;
import static com.nu.art.cyborg.modules.LocationModule.LocationService.OFFLINE;

/**
 * Created by TacB0sS on 15-Sep 2017.
 */
@ModuleDescriptor(usesPermissions = {
	permission.ACCESS_COARSE_LOCATION,
	permission.ACCESS_FINE_LOCATION
})
public class LocationModule
	extends CyborgModule
	implements LocationListener {

	private long minTime;

	private float minDistance;

	private LocationService currentService = OFFLINE;

	enum LocationService {
		GPS,
		NETWORK,
		OFFLINE
	}

	@Override
	public void onLocationChanged(final Location location) {
		logInfo("location: onLocationChanged");
		dispatchGlobalEvent("update location to: " + location, new Processor<OnLocationUpdatedListener>() {
			@Override
			public void process(OnLocationUpdatedListener listener) {
				listener.onLocationUpdated(location);
			}
		});
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		String statusText = null;
		switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
				statusText = "Out Of Service";
				break;

			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				statusText = "Temporarily Unavailable";
				break;

			case LocationProvider.AVAILABLE:
				statusText = "Available";
				break;
		}

		logInfo("location: onStatusChanged - " + provider + " - " + statusText);
	}

	@Override
	@SuppressWarnings("MissingPermission")
	public void onProviderEnabled(String provider) {
		logInfo("location: onProviderEnabled - " + provider);
		checkAndSetProviders();
	}

	@Override
	@SuppressWarnings("MissingPermission")
	public void onProviderDisabled(String provider) {
		logInfo("location: onProviderDisabled - " + provider);
		checkAndSetProviders();
	}

	public interface OnLocationUpdatedListener {

		void onLocationUpdated(Location location);

		void onLocationUpdateError();
	}

	private LocationManager locationManager;

	@Override
	protected void init() {
		locationManager = getSystemService(LocationService);
	}

	@SuppressWarnings("MissingPermission")
	private void checkAndSetProviders() {
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
			currentService = GPS;
			disableLooper();
		} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
			currentService = NETWORK;
			activateLooper();
		} else {
			if (currentService == GPS)
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
			currentService = OFFLINE;
			activateLooper();
		}
	}

	private Runnable locationUpdate = new Runnable() {
		@Override
		public void run() {
			updateLastLocation();
			postOnUI(minTime, this);
		}
	};

	private void activateLooper() {
		disableLooper();
		postOnUI(1000, locationUpdate);
	}

	private void disableLooper() {
		removeActionFromUI(locationUpdate);
	}

	@SuppressWarnings("MissingPermission")
	public void requestLocationUpdates(long minTime, float minDistance) {
		this.minTime = minTime;
		this.minDistance = minDistance;

		checkAndSetProviders();
		if (currentService == OFFLINE)
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);
	}

	public void removeLocationUpdates() {
		locationManager.removeUpdates(this);
	}

	@SuppressWarnings("MissingPermission")
	public void updateLastLocation() {
		logInfo("location: network/offline");
		List<String> providers = locationManager.getProviders(true);
		Location bestLocation = null;
		for (String provider : providers) {
			Location location = locationManager.getLastKnownLocation(provider);
			if (location == null) {
				continue;
			}
			if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
				// Found best last known location: %s", location);
				bestLocation = location;
			}
		}

		if (bestLocation == null) {
			toastDebug("Could not find location");
			dispatchGlobalEvent("location update error", new Processor<OnLocationUpdatedListener>() {
				@Override
				public void process(OnLocationUpdatedListener listener) {
					listener.onLocationUpdateError();
				}
			});
			return;
		}

		final Location location = bestLocation;
		dispatchGlobalEvent("update location to: " + location, new Processor<OnLocationUpdatedListener>() {
			@Override
			public void process(OnLocationUpdatedListener listener) {
				listener.onLocationUpdated(location);
			}
		});
	}
}

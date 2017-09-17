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

package com.nu.art.cyborg.modules;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

import java.util.List;

/**
 * Created by TacB0sS on 15-Sep 2017.
 */
@ModuleDescriptor(usesPermissions = {
		Manifest.permission.ACCESS_COARSE_LOCATION,
		Manifest.permission.ACCESS_FINE_LOCATION
})
public class LocationModule
		extends CyborgModule {

	public interface OnLocationUpdatedListener {

		void onLocationUpdated(Location location);
	}

	private LocationManager locationManager;

	@Override
	protected void init() {
		locationManager = getSystemService(LocationService);
	}

	@SuppressWarnings("MissingPermission")
	public void updateCurrentLocation() {
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
			return;
		}

		final Location location = bestLocation;
		dispatchGlobalEvent("update location to: " + location, OnLocationUpdatedListener.class, new Processor<OnLocationUpdatedListener>() {
			@Override
			public void process(OnLocationUpdatedListener listener) {
				listener.onLocationUpdated(location);
			}
		});
	}
}

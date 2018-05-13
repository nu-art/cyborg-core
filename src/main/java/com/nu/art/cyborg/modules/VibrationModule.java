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
import android.annotation.SuppressLint;
import android.os.Vibrator;

import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.PreferencesModule;
import com.nu.art.cyborg.core.modules.PreferencesModule.PreferenceKey;

@ModuleDescriptor(usesPermissions = {"?" + permission.VIBRATE})
@SuppressLint("MissingPermission")
public final class VibrationModule
	extends CyborgModule {

	private PreferenceKey<Boolean> VibrationState;

	private boolean vibration;

	private Vibrator vibrator;

	@Override
	protected void init() {
		PreferencesModule preferences = getModule(PreferencesModule.class);
		vibrator = getSystemService(VibratorService);

		if (!getModule(PermissionModule.class).isPermissionGranted(permission.VIBRATE)) {
			vibration = false;
			logDebug("Vibration permission is not in manifest!!");
			VibrationState = preferences.new BooleanPreference("VibrationState", false);
		} else {
			VibrationState = preferences.new BooleanPreference("VibrationState", true);
			VibrationState.set(true);
		}
	}

	public final void setVibrationEnabled(boolean enabled) {
		vibration = enabled;
		VibrationState.set(vibration);
	}

	public final void vibrateImpl(int repeat, long... intervals) {
		if (!vibration)
			return;

		vibrator.vibrate(intervals, repeat);
	}

	public final void vibrateImpl(long interval) {
		if (!vibration)
			return;

		vibrator.vibrate(interval);
	}
}

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

package com.nu.art.cyborg.core.modules;

import android.provider.Settings.Secure;
import android.view.Display;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.common.consts.DeviceScreenDensity;
import com.nu.art.cyborg.common.consts.DeviceScreenSize;
import com.nu.art.cyborg.common.consts.DeviceValuesFolder;
import com.nu.art.cyborg.common.consts.ScreenOrientation;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.crashReport.CrashReportListener;

import java.io.File;
import java.util.HashMap;

@ModuleDescriptor(usesPermissions = {})
public final class DeviceDetailsModule
		extends CyborgModule
		implements CrashReportListener {

	private DeviceScreenSize screenSize;

	private DeviceScreenDensity screenDensity;

	private DeviceValuesFolder deviceValues;

	private String androidId;

	@Override
	protected void init() {
		int screenLayout = cyborg.getResources().getConfiguration().screenLayout;
		screenSize = DeviceScreenSize.getValueByScreenLayout(screenLayout);

		float density = cyborg.getResources().getDisplayMetrics().density;
		screenDensity = DeviceScreenDensity.getValueByDensity(density);

		String resolution = cyborg.getString(R.string.Resolution);

		try {
			deviceValues = DeviceValuesFolder.valueOf(resolution);
		} catch (Exception e) {
			deviceValues = DeviceValuesFolder.UNKNOWN;
		}
		androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	}

	@Override
	protected void printModuleDetails() {
		logInfo("    Android Device Id: " + androidId);
		logInfo("    Device values folder: " + deviceValues.getFolderName());
		logInfo("    Screen Density: " + screenDensity.name());
		logInfo("    Screen Size: " + screenSize.name());
	}

	public final boolean isSuperUser() {
		File pathToSuperUserFile = new File("/system/xbin/su");
		return pathToSuperUserFile.exists();
	}

	public final String getAndroidDeviceId() {
		return androidId;
	}

	@Override
	public void onApplicationCrashed(HashMap<String, Object> moduleCrashData) {
		moduleCrashData.put("androidId", androidId);
		moduleCrashData.put("ScreenDpi", screenDensity.name());
		moduleCrashData.put("ScreenSize", screenSize.name());
		moduleCrashData.put("isRooted", isSuperUser());
	}

	public ScreenOrientation getOrientation() {
		Display display = getSystemService(WindowService).getDefaultDisplay();
		if (display.getWidth() == display.getHeight()) {
			return ScreenOrientation.Square;
		}
		if (display.getWidth() < display.getHeight()) {
			return ScreenOrientation.Portrait;
		}
		return ScreenOrientation.Landscape;
	}
}

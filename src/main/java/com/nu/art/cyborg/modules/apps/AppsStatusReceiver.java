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

package com.nu.art.cyborg.modules.apps;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.nu.art.cyborg.core.CyborgReceiver;

public class AppsStatusReceiver
	extends CyborgReceiver<AppsStatusModule> {

	public AppsStatusReceiver() {
		super(AppsStatusModule.class);
	}

	@Override
	protected void onReceive(Intent intent, AppsStatusModule module) {
		String action = intent.getAction();
		boolean isUninstallEvent = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
		boolean isUpdate = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

		logDebug("action: " + action);
		logDebug("isUninstallEvent: " + isUninstallEvent);
		logDebug("isUpdate: " + isUpdate);

		String packageName = getPackageName(intent);
		if (packageName == null)
			return;
		logDebug("packageName: " + packageName);

		if (action == null) {
			logWarning("No action...");
			return;
		}

		switch (action) {
			case Intent.ACTION_PACKAGE_REMOVED:
				module.onAppRemoved(packageName, isUninstallEvent, isUpdate);
				return;

			case Intent.ACTION_PACKAGE_ADDED:
				module.onAppInstalled(packageName, isUpdate);
				break;

			case Intent.ACTION_PACKAGE_REPLACED:
				module.onAppUpdated(packageName);
				break;
		}
	}

	@Nullable
	private String getPackageName(Intent intent) {
		Uri dataUri = intent.getData();
		if (dataUri == null) {
			logWarning("No package information...");
			return null;
		}
		logDebug("uri: " + dataUri.toString());

		String packageName = dataUri.toString().replace("package:", "");
		if (packageName.equals(cyborg.getPackageName()))
			return null;
		return packageName;
	}
}
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

package com.nu.art.cyborg.core.modules;

import com.nu.art.cyborg.core.CyborgModule;

public abstract class UpgradeModule
	extends CyborgModule {

	@Override
	protected final void init() {

		onFirstLaunch();
		int fromVersion = 0;
		int toVersion = 0;
		onUpgrade(fromVersion, toVersion);
	}

	protected abstract void onUpgrade(int fromVersion, int toVersion);

	protected abstract void onFirstLaunch();

	// @Override
	// public void onApplicationInstalled() {
	// File mntFolder = new File("/mnt");
	// if (!mntFolder.exists()) {
	// sendEvent("TESTING SD-Card Folders", "Not Exists", Build.MANUFACTURER + "-" + Build.MODEL + "-" +
	// VERSION.SDK_INT, 1);
	// return;
	// }
	//
	// File[] files = mntFolder.listFiles();
	// if (files == null) {
	// sendEvent("TESTING SD-Card Folders", "Empty", Build.MANUFACTURER + "-" + Build.MODEL + "-" + VERSION.SDK_INT, 1);
	// return;
	// }
	//
	// for (File file : files) {
	// sendEvent("TESTING SD-Card Folders", "/mnt/" + file.getName(), Build.MANUFACTURER + "-" + Build.MODEL + "-" +
	// VERSION.SDK_INT, 1);
	// }
	// }
}

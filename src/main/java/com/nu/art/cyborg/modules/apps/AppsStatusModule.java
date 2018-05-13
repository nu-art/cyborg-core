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

import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.CyborgModule;

/**
 * Created by tacb0ss on 14/05/2018.
 */

public class AppsStatusModule
	extends CyborgModule {

	public interface AppsListener {

		void onApplicationUninstalled(String packageName, boolean hasClearedData);

		void onApplicationInstalled(String packageName);

		void onApplicationUpdated(String packageName);
	}

	private String[] toBeUpdated = {};

	@Override
	protected void init() {

	}

	final void onAppRemoved(final String packageName, final boolean isUninstallEvent, boolean isUpdate) {
		if (isUpdate) {
			toBeUpdated = ArrayTools.appendElement(toBeUpdated, packageName);
			logDebug("App is being removed for update: " + packageName);
			return;
		}

		dispatchGlobalEvent("App Uninstalled: " + packageName + ", " + (isUninstallEvent ? "Cleared Data" : "Data Preserved"), new Processor<AppsListener>() {
			@Override
			public void process(AppsListener listener) {
				listener.onApplicationUninstalled(packageName, isUninstallEvent);
			}
		});
	}

	final void onAppInstalled(final String packageName, boolean isUpdate) {
		if (isUpdate) {
			toBeUpdated = ArrayTools.removeElement(toBeUpdated, packageName);
			logDebug("App is being installed for update: " + packageName);
			return;
		}

		dispatchGlobalEvent("App Installed: " + packageName, new Processor<AppsListener>() {
			@Override
			public void process(AppsListener listener) {
				listener.onApplicationInstalled(packageName);
			}
		});
	}

	final void onAppUpdated(final String packageName) {
		dispatchGlobalEvent("App Updated: " + packageName, new Processor<AppsListener>() {
			@Override
			public void process(AppsListener listener) {
				listener.onApplicationUpdated(packageName);
			}
		});
	}
}

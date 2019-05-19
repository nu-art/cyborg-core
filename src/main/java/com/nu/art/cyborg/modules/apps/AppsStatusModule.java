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

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.cyborg.core.CyborgModule;

import java.io.File;
import java.io.IOException;

/**
 * Created by TacB0sS on 14/05/2018.
 */

public class AppsStatusModule
	extends CyborgModule {

	public interface OnAppInstalled {

		void onApplicationInstalled(String packageName);
	}

	public interface AppsListener
		extends OnAppInstalled {

		void onApplicationUninstalled(String packageName, boolean hasClearedData);

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

		dispatchGlobalEvent("App Uninstalled: " + packageName + ", " + (isUninstallEvent ? "Cleared Data"
		                                                                                 : "Data Preserved"), AppsListener.class, new Processor<AppsListener>() {
			@Override
			public void process(AppsListener listener) {
				listener.onApplicationUninstalled(packageName, isUninstallEvent);
			}
		});
	}

	public final void installApkSync(File apkFile)
		throws InstallationException {
		if (!apkFile.exists())
			throw new InstallationException("apk file does not exists: " + apkFile.getAbsolutePath());

		String command = "adb install -r " + apkFile.getAbsolutePath();
		try {
			String[] cmdArray = {
				"su",
				"-c",
				command
			};
			Process process = Runtime.getRuntime().exec(cmdArray);
			int responseCode = process.waitFor();
			if (responseCode != 0) {
				String errorStream = StreamTools.readFullyAsString(process.getErrorStream());
				throw new InstallationException("Failed installing apk: " + apkFile.getAbsolutePath() + "\n" + errorStream);
			}
		} catch (IOException e) {
			throw new InstallationException("apk file does not exists: " + apkFile.getAbsolutePath(), e);
		} catch (InterruptedException e) {
			throw new InstallationException("apk file does not exists: " + apkFile.getAbsolutePath(), e);
		}
	}

	final void onAppInstalled(final String packageName, boolean isUpdate) {
		if (isUpdate) {
			toBeUpdated = ArrayTools.removeElement(toBeUpdated, packageName);
			logDebug("App is being installed for update: " + packageName);
			return;
		}

		dispatchGlobalEvent("App Installed: " + packageName, AppsListener.class, new Processor<AppsListener>() {
			@Override
			public void process(AppsListener listener) {
				listener.onApplicationInstalled(packageName);
			}
		});
	}

	final void onAppUpdated(final String packageName) {
		dispatchGlobalEvent("App Updated: " + packageName, AppsListener.class, new Processor<AppsListener>() {
			@Override
			public void process(AppsListener listener) {
				listener.onApplicationUpdated(packageName);
			}
		});
	}
}

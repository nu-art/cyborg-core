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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgActivityBridge;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ModuleDescriptor
public class PermissionModule
	extends CyborgModule {

	public interface PermissionResultListener {

		void onPermissionsRejected(int requestCode, String[] rejected);

		void onAllPermissionsGranted(int requestCode);
	}

	public final String[] getRejectedPermissions(String... requestedPermissions) {
		ArrayList<String> notGranted = new ArrayList<>();
		for (String permission : requestedPermissions) {
			if (!isPermissionGranted(permission))
				notGranted.add(permission);
		}

		return ArrayTools.asArray(notGranted, String.class);
	}

	public final boolean isPermissionGranted(String permission) {
		return ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	protected void init() {

	}

	public void requestPermission(final int requestCode, final String... permissions) {
		String[] rejectedPermissions = getRejectedPermissions(permissions);

		if (rejectedPermissions.length == 0) {
			dispatchGlobalEvent("All Permissions Granted", PermissionResultListener.class, new Processor<PermissionResultListener>() {
				@Override
				public void process(PermissionResultListener listener) {
					listener.onAllPermissionsGranted(requestCode);
				}
			});
			return;
		}

		List<String> permissionsInManifest;
		try {
			PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
			permissionsInManifest = Arrays.asList(packageInfo.requestedPermissions);
		} catch (NameNotFoundException e) {
			throw new MUST_NeverHappenException("", e);
		}

		for (String rejectedPermission : rejectedPermissions) {
			if (permissionsInManifest.contains(rejectedPermission))
				continue;

			throw ExceptionGenerator.permissionMissingInManifest(rejectedPermission);
		}

		postActivityAction(new ActivityStackAction() {
			@Override
			public void execute(CyborgActivityBridge bridge) {
				ActivityCompat.requestPermissions(bridge.getContext(), permissions, requestCode);
			}
		});
	}

	public void onPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		final ArrayList<String> rejected = new ArrayList<>();

		for (int i = 0; i < grantResults.length; i++) {
			if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
				rejected.add(permissions[i]);
			}
		}

		String message = "Permissions request: " + (grantResults.length - rejected.size()) + "/" + grantResults.length + " Granted";
		dispatchGlobalEvent(message, PermissionResultListener.class, new Processor<PermissionResultListener>() {
			@Override
			public void process(PermissionResultListener listener) {
				if (rejected.size() > 0)
					listener.onPermissionsRejected(requestCode, ArrayTools.asArray(rejected, String.class));
				else
					listener.onAllPermissionsGranted(requestCode);
			}
		});
	}
}

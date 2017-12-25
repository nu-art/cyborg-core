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

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgActivityBridge;
import com.nu.art.cyborg.core.CyborgModule;

import java.util.ArrayList;

@ModuleDescriptor
public class PermissionModule
		extends CyborgModule {

	private static final int RequestCode_Permissions = 100;

	public interface PermissionResultListener {

		void onPermissionsRejected(int requestCode, String[] rejected);

		void onAllPermissionsGranted(int requestCode);
	}

	public final String[] getRejectedPermissions(String... requestedPermissions) {
		ArrayList<String> notGranted = new ArrayList<>();
		for (String permission : requestedPermissions) {
			if (!isGranted(permission))
				notGranted.add(permission);
		}

		return ArrayTools.asArray(notGranted, String.class);
	}

	private boolean isGranted(String permission) {
		return ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	protected void init() {

	}

	public void requestPermission(final int requestCode, final String... permissions) {
		String[] rejectedPermissions = getRejectedPermissions(permissions);

		if (rejectedPermissions.length == 0) {
			dispatchModuleEvent("All Permissions Granted", new Processor<PermissionResultListener>() {
				@Override
				public void process(PermissionResultListener listener) {
					listener.onAllPermissionsGranted(requestCode);
				}
			});
			return;
		}

		postActivityAction(new ActivityStackAction() {
			@Override
			public void execute(CyborgActivityBridge bridge) {
				ActivityCompat.requestPermissions(bridge.getActivity(), permissions, requestCode);
			}
		});
	}

	public boolean onPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case RequestCode_Permissions: {
				final ArrayList<String> rejected = new ArrayList<>();

				for (int i = 0; i < grantResults.length; i++) {
					if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
						rejected.add(permissions[i]);
					}
				}

				String message = "Permissions request: " + (grantResults.length - rejected.size()) + "/" + grantResults.length + " Granted";
				dispatchModuleEvent(message, new Processor<PermissionResultListener>() {
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
		return true;
	}
}

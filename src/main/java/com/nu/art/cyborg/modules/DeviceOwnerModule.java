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

import android.app.admin.DevicePolicyManager;

import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

/**
 * Created by TacB0sS on 28-Feb 2017.
 */
@ModuleDescriptor(usesPermissions = {},
                  dependencies = {AppDetailsModule.class})
public class DeviceOwnerModule
	extends CyborgModule {

	public interface OnDeviceOwnerListener {
	}

	@Override
	protected void init() {
		DevicePolicyManager devicePolicyManager = getSystemService(PolicyManagerService);
	}
}

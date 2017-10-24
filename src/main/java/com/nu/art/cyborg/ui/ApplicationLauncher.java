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

package com.nu.art.cyborg.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.core.CyborgActivityBridgeImpl;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.CyborgBuilder.LaunchConfiguration;

/**
 * This is the default application launcher Activity when using Cyborg!
 */
public final class ApplicationLauncher
		extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LaunchConfiguration launchConfiguration = CyborgBuilder.getInstance().getLaunchConfiguration();
		if (launchConfiguration == null)
			throw new BadImplementationException("If you want to use the launching configuration feature, you must specify a layout when creating Cyborg, see documentation of this class");
		Intent intent = CyborgActivityBridgeImpl.composeIntent(launchConfiguration);

		if (getIntent() != null && getIntent().getExtras() != null)
			intent.putExtras(getIntent().getExtras());

		startActivity(intent);
		finish();
	}
}

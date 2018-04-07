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

package com.nu.art.cyborg.common.consts;

import android.os.Build.VERSION;

public enum BarHidingType {
	LowProfile(1, 11),
	Hide(2, 14),
	FullScreen(4, 16),
	UNDOCUMENTED(8, 16),;

	private int value;

	private int minSdkCode;

	private BarHidingType(int value, int sdkCode) {
		this.value = value;
		this.minSdkCode = sdkCode;
	}

	public int getValue() {
		return value;
	}

	public boolean isApplicable() {
		return VERSION.SDK_INT >= minSdkCode;
	}
}

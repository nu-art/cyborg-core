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

package com.nu.art.cyborg.common.consts;

import android.content.res.Configuration;

/**
 * Derived from here: <br>
 * http://stackoverflow.com/questions/3166501/getting-the-screen-density-programmatically-in-android
 *
 * @author TacB0sS
 */
public enum DeviceScreenSize {
	Undefined(Configuration.SCREENLAYOUT_LONG_UNDEFINED),
	Small(Configuration.SCREENLAYOUT_SIZE_SMALL),
	Normal(Configuration.SCREENLAYOUT_SIZE_NORMAL),
	Large(Configuration.SCREENLAYOUT_SIZE_LARGE),
	XLarge(4),
	TV(-1),;

	private int value;

	private DeviceScreenSize(int value) {
		this.value = value;
	}

	public static final DeviceScreenSize getValueByScreenLayout(int value) {
		for (DeviceScreenSize size : values()) {
			if ((value & Configuration.SCREENLAYOUT_SIZE_MASK) == size.value) {
				return size;
			}
		}
		return Undefined;
	}
}

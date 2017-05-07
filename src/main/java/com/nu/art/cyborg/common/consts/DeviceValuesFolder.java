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

/**
 * Derived from here: <br>
 * http://stackoverflow.com/questions/3166501/getting-the-screen-density-programmatically-in-android
 *
 * @author TacB0sS
 */
public enum DeviceValuesFolder {
	UNKNOWN("values"),
	LDPI("values-ldpi"),
	MDPI("values-mdpi"),
	HDPI("values-hdpi"),
	XHDPI("values-xhdpi"),
	XXHDPI("values-xxhdpi"),
	XXXHDPI("values-xxxhdpi"),
	SW320DP("values-sw320dp"),
	SW480DP("values-sw480dp"),
	SW600DP("values-sw600dp"),
	SW720DP("values-sw720dp"),;

	private final String folderName;

	private DeviceValuesFolder(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}

}

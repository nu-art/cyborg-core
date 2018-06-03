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

package com.nu.art.cyborg.core.modules.preferences;

import com.nu.art.cyborg.core.modules.preferences.PreferencesModule.SharedPrefs;

public final class StringPreference
	extends PreferenceKey<String> {

	public StringPreference(String key, String defaultValue) {
		super(key, defaultValue);
	}

	public StringPreference(String key, String defaultValue, String storageGroup) {
		super(key, defaultValue, storageGroup);
	}

	public StringPreference(String key, String defaultValue, long expires) {
		super(key, defaultValue, expires);
	}

	public StringPreference(String key, String defaultValue, long expires, String storageGroup) {
		super(key, defaultValue, expires, storageGroup);
	}

	@Override
	protected String _get(SharedPrefs preferences, String key, String defaultValue) {
		return preferences.get(key, defaultValue);
	}

	@Override
	protected void _set(SharedPrefs preferences, String key, String value) {
		preferences.put(key, value);
	}
}
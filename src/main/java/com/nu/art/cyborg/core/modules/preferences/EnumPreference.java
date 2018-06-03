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

import android.support.annotation.NonNull;

import com.nu.art.core.interfaces.Getter;

public final class EnumPreference<EnumType extends Enum<EnumType>>
	implements Getter<EnumType> {

	private final StringPreference key;

	private final Class<EnumType> enumType;

	public EnumPreference(String key, Class<EnumType> enumType, EnumType defaultValue) {
		this(key, enumType, defaultValue, null);
	}

	public EnumPreference(String key, Class<EnumType> enumType, EnumType defaultValue, long expires) {
		this(key, enumType, defaultValue, expires, null);
	}

	public EnumPreference(String key, Class<EnumType> enumType, EnumType defaultValue, String storageGroup) {
		this(key, enumType, defaultValue, -1, storageGroup);
	}

	@SuppressWarnings("unchecked")
	public EnumPreference(String key, Class<EnumType> enumType, EnumType defaultValue, long expires, String storageGroup) {
		this.key = new StringPreference(key, defaultValue == null ? null : defaultValue.name(), expires, storageGroup);
		this.enumType = enumType;
	}

	public EnumType get() {
		return get(true);
	}

	@NonNull
	@SuppressWarnings("ConstantConditions")
	public EnumType get(boolean printToLog) {
		String value = key.get(printToLog);
		if (value == null)
			return null;

		try {
			return Enum.valueOf(enumType, value);
		} catch (Exception e) {
			key.delete();
			return get();
		}
	}

	public void set(EnumType value) {
		key.set(value.name());
	}

	public void delete() {
		key.delete();
	}
}

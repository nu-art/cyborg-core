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

package com.nu.art.cyborg.core.modules;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;

import com.nu.art.core.interfaces.Getter;
import com.nu.art.core.interfaces.Serializer;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;

import java.lang.reflect.Type;
import java.util.HashMap;

@SuppressWarnings( {
											 "unused",
											 "WeakerAccess"
									 })
@ModuleDescriptor(usesPermissions = {})
public final class PreferencesModule
		extends CyborgModule {

	private static final String DefaultStorageGroup = "DefaultStorage";

	private static final String EXPIRES_POSTFIX = "-Expires";

	private HashMap<String, SharedPreferences> preferencesMap = new HashMap<>();

	private PreferencesModule() {}

	@Override
	protected void init() {}

	public final void clearExpiration(PreferenceKey type) {
		Editor editor = getPreferences(type.storageGroup).edit();
		editor.putLong(type.key + EXPIRES_POSTFIX, -1L);
		editor.apply();
	}

	public final void removeValue(PreferenceKey<?> type) {
		Editor editor = getPreferences(type.storageGroup).edit();
		editor.remove(type.key);
		editor.apply();
	}

	public void clearCache() {
		for (String key : preferencesMap.keySet()) {
			dropPreferences(key);
		}
	}

	public void dropPreferences(String storageGroup) {
		getPreferences(storageGroup).edit().clear().apply();
	}

	private SharedPreferences getPreferences(String storageGroup) {
		if (storageGroup == null)
			storageGroup = DefaultStorageGroup;

		SharedPreferences sharedPreferences = preferencesMap.get(storageGroup);
		if (sharedPreferences == null) {
			sharedPreferences = cyborg.getApplicationContext().getSharedPreferences(storageGroup, 0);
			preferencesMap.put(storageGroup, sharedPreferences);
		}
		return sharedPreferences;
	}

	public abstract class PreferenceKey<ItemType>
			implements Getter<ItemType> {

		final String key;

		private final String storageGroup;

		private final ItemType defaultValue;

		private final long expires;

		public PreferenceKey(String key, ItemType defaultValue) {
			this(key, defaultValue, -1);
		}

		public PreferenceKey(String key, ItemType defaultValue, String storageGroup) {
			this(key, defaultValue, -1, storageGroup);
		}

		public PreferenceKey(String key, ItemType defaultValue, long expires) {
			this(key, defaultValue, expires, null);
		}

		public PreferenceKey(String key, ItemType defaultValue, long expires, String storageGroup) {
			this.key = key;
			this.storageGroup = storageGroup;
			this.defaultValue = defaultValue;
			this.expires = expires;
		}

		public final ItemType get() {
			return get(true);
		}

		public final ItemType get(boolean printToLog) {
			SharedPreferences preferences = getPreferences(storageGroup);
			ItemType cache;
			if (expires == -1 || System.currentTimeMillis() - preferences.getLong(key + EXPIRES_POSTFIX, -1) < expires) {
				cache = _get(preferences, key, defaultValue);
				if (printToLog)
					logInfo("+----+ LOADED: " + key + ": " + cache);
				return cache;
			} else {
				cache = defaultValue;
				if (printToLog)
					logInfo("+----+ DEFAULT: " + key + ": " + cache);
				return cache;
			}
		}

		protected abstract ItemType _get(SharedPreferences preferences, String key, ItemType defaultValue);

		public void set(ItemType value) {
			set(value, true);
		}

		public void set(ItemType value, boolean printToLog) {
			Editor editor = getPreferences(storageGroup).edit();
			logDebug("+----+ SET: " + key + ": " + value);

			_set(editor, key, value);
			if (expires != -1)
				editor.putLong(key + EXPIRES_POSTFIX, System.currentTimeMillis());
			editor.apply();
		}

		protected abstract void _set(Editor preferences, String key, ItemType value);

		public final void clearExpiration() {
			PreferencesModule.this.clearExpiration(this);
		}

		public final void delete() {
			clearExpiration();
			removeValue(this);
		}
	}

	public final class IntegerPreference
			extends PreferenceKey<Integer> {

		public IntegerPreference(String key, int defaultValue) {
			super(key, defaultValue);
		}

		public IntegerPreference(String key, int defaultValue, String storageGroup) {
			super(key, defaultValue, storageGroup);
		}

		public IntegerPreference(String key, int defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public IntegerPreference(String key, int defaultValue, long expires, String storageGroup) {
			super(key, defaultValue, expires, storageGroup);
		}

		@Override
		protected Integer _get(SharedPreferences preferences, String key, Integer defaultValue) {
			return preferences.getInt(key, defaultValue);
		}

		@Override
		protected void _set(Editor preferences, String key, Integer value) {
			preferences.putInt(key, value);
		}
	}

	public final class BooleanPreference
			extends PreferenceKey<Boolean> {

		public BooleanPreference(String key, boolean defaultValue) {
			super(key, defaultValue);
		}

		public BooleanPreference(String key, boolean defaultValue, String storageGroup) {
			super(key, defaultValue, storageGroup);
		}

		public BooleanPreference(String key, boolean defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public BooleanPreference(String key, boolean defaultValue, long expires, String storageGroup) {
			super(key, defaultValue, expires, storageGroup);
		}

		@Override
		protected Boolean _get(SharedPreferences preferences, String key, Boolean defaultValue) {
			return preferences.getBoolean(key, defaultValue);
		}

		@Override
		protected void _set(Editor preferences, String key, Boolean value) {
			preferences.putBoolean(key, value);
		}
	}

	public final class LongPreference
			extends PreferenceKey<Long> {

		public LongPreference(String key, long defaultValue) {
			super(key, defaultValue);
		}

		public LongPreference(String key, long defaultValue, String storageGroup) {
			super(key, defaultValue, storageGroup);
		}

		public LongPreference(String key, long defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public LongPreference(String key, long defaultValue, long expires, String storageGroup) {
			super(key, defaultValue, expires, storageGroup);
		}

		@Override
		protected Long _get(SharedPreferences preferences, String key, Long defaultValue) {
			return preferences.getLong(key, defaultValue);
		}

		@Override
		protected void _set(Editor preferences, String key, Long value) {
			preferences.putLong(key, value);
		}
	}

	public final class FloatPreference
			extends PreferenceKey<Float> {

		public FloatPreference(String key, float defaultValue) {
			super(key, defaultValue);
		}

		public FloatPreference(String key, float defaultValue, String storageGroup) {
			super(key, defaultValue, storageGroup);
		}

		public FloatPreference(String key, float defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public FloatPreference(String key, float defaultValue, long expires, String storageGroup) {
			super(key, defaultValue, expires, storageGroup);
		}

		@Override
		protected Float _get(SharedPreferences preferences, String key, Float defaultValue) {
			return preferences.getFloat(key, defaultValue);
		}

		@Override
		protected void _set(Editor preferences, String key, Float value) {
			preferences.putFloat(key, value);
		}
	}

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
		protected String _get(SharedPreferences preferences, String key, String defaultValue) {
			return preferences.getString(key, defaultValue);
		}

		@Override
		protected void _set(Editor preferences, String key, String value) {
			preferences.putString(key, value);
		}
	}

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
			clearExpiration(key);
			removeValue(key);
		}
	}

	public final class CustomPreference<ItemType>
			implements Getter<ItemType> {

		private ItemType cache;

		private final StringPreference key;

		private final Type itemType;

		private final ItemType defaultValue;

		private final Serializer<Object, String> serializer;

		public CustomPreference(Serializer<Object, String> serializer, String key, Type type) {
			this(serializer, key, type, (ItemType) null);
		}

		public CustomPreference(Serializer<Object, String> serializer, String key, Type type, long expires) {
			this(serializer, key, type, null, expires, null);
		}

		public CustomPreference(Serializer<Object, String> serializer, String key, Type type, String storageGroup) {
			this(serializer, key, type, null, -1, storageGroup);
		}

		public CustomPreference(Serializer<Object, String> serializer, String key, Type type, ItemType defaultValue) {
			this(serializer, key, type, defaultValue, null);
		}

		public CustomPreference(Serializer<Object, String> serializer, String key, Type type, ItemType defaultValue, long expires) {
			this(serializer, key, type, defaultValue, expires, null);
		}

		public CustomPreference(Serializer<Object, String> serializer, String key, Type type, ItemType defaultValue, String storageGroup) {
			this(serializer, key, type, defaultValue, -1, storageGroup);
		}

		@SuppressWarnings("unchecked")
		public CustomPreference(Serializer<Object, String> serializer, String key, Type type, ItemType defaultValue, long expires, String storageGroup) {
			this.key = new StringPreference(key, null, expires, storageGroup);
			this.defaultValue = defaultValue;
			this.serializer = serializer;
			itemType = type;
		}

		public ItemType get() {
			return get(true);
		}

		@SuppressWarnings("unchecked")
		public ItemType get(boolean printToLog) {
			if (cache != null) {
				if (printToLog)
					logDebug("+----+ CACHED: " + key.key + ": " + cache);
				return cache;
			}

			String value = key.get(printToLog);
			if (value == null) {
				cache = defaultValue;
				if (printToLog)
					logDebug("+----+ DEFAULT: " + key.key + ": " + cache);
				return cache;
			}

			try {
				cache = (ItemType) serializer.deserialize(value, itemType);
			} catch (Exception e) {
				logError("Error while deserializing item type: " + itemType + ", probably changed class structure... returning null", e);
				cache = null;
			}

			if (printToLog)
				logInfo("+----+ DESERIALIZED: " + key.key + ": " + cache);
			return cache;
		}

		public void set(ItemType value) {
			set(value, true);
		}

		public void set(ItemType value, boolean printToLog) {
			String valueAsString = value == null ? null : serializer.serialize(value);
			cache = value;
			key.set(valueAsString, printToLog);
		}

		public void delete() {
			clearExpiration(key);
			removeValue(key);
			cache = null;
		}
	}
}

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

import com.nu.art.core.generics.Function;
import com.nu.art.core.interfaces.Getter;
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

	public interface PreferencesStorage {

		String getPreferencesName();

		int getMode();
	}

	public enum PreferencesType
			implements PreferencesStorage {
		Default("DefaultStorage"/* "Configuration" */, 0);

		private String preferencesName;

		private int mode;

		PreferencesType(String name, int mode) {
			this.preferencesName = name;
			this.mode = mode;
		}

		@Override
		public String getPreferencesName() {
			return preferencesName;
		}

		@Override
		public int getMode() {
			return mode;
		}

	}

	private static final String EXPIRES_POSTFIX = "-Expires";

	private final PreferencesStorage DefaultPreferences = PreferencesType.Default;

	private HashMap<PreferencesStorage, SharedPreferences> preferencesMap = new HashMap<>();

	private PreferencesModule() {}

	@Override
	protected void init() {}

	public final void clearExpiration(PreferenceKey type) {
		Editor editor = getPreferences(type.type).edit();
		editor.putLong(type.key + EXPIRES_POSTFIX, -1L);
		editor.apply();
	}

	public final void removeValue(PreferenceKey<?> type) {
		Editor editor = getPreferences(type.type).edit();
		editor.remove(type.key);
		editor.apply();
	}

	public void clearCache() {
		for (PreferencesStorage key : preferencesMap.keySet()) {
			dropPreferences(key);
		}
	}

	public void dropPreferences(PreferencesStorage type) {
		getPreferences(type).edit().clear().apply();
	}

	private SharedPreferences getPreferences(PreferencesStorage type) {
		if (type == null)
			type = DefaultPreferences;

		SharedPreferences sharedPreferences = preferencesMap.get(type);
		if (sharedPreferences == null) {
			sharedPreferences = cyborg.getApplicationContext().getSharedPreferences(type.getPreferencesName(), type.getMode());
			preferencesMap.put(type, sharedPreferences);
		}
		return sharedPreferences;
	}

	public abstract class PreferenceKey<ItemType>
			implements Getter<ItemType> {

		final String key;

		private final PreferencesStorage type;

		private final ItemType defaultValue;

		private final long expires;

		public PreferenceKey(String key, ItemType defaultValue) {
			this(key, defaultValue, -1);
		}

		public PreferenceKey(String key, ItemType defaultValue, PreferencesStorage type) {
			this(key, defaultValue, -1, type);
		}

		public PreferenceKey(String key, ItemType defaultValue, long expires) {
			this(key, defaultValue, expires, null);
		}

		public PreferenceKey(String key, ItemType defaultValue, long expires, PreferencesStorage type) {
			this.key = key;
			this.type = type;
			this.defaultValue = defaultValue;
			this.expires = expires;
		}

		public final ItemType get() {
			SharedPreferences preferences = getPreferences(type);
			ItemType cache;
			if (expires == -1 || System.currentTimeMillis() - preferences.getLong(key + EXPIRES_POSTFIX, -1) < expires) {
				cache = _get(preferences, key, defaultValue);
				logInfo("+----+ LOADED: " + key + ": " + cache);
				return cache;
			} else {
				cache = defaultValue;
				logInfo("+----+ DEFAULT: " + key + ": " + cache);
				return cache;
			}
		}

		protected abstract ItemType _get(SharedPreferences preferences, String key, ItemType defaultValue);

		public void set(ItemType value) {
			Editor editor = getPreferences(type).edit();
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

		public IntegerPreference(String key, int defaultValue, PreferencesStorage type) {
			super(key, defaultValue, type);
		}

		public IntegerPreference(String key, int defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public IntegerPreference(String key, int defaultValue, long expires, PreferencesStorage type) {
			super(key, defaultValue, expires, type);
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

		public BooleanPreference(String key, boolean defaultValue, PreferencesStorage type) {
			super(key, defaultValue, type);
		}

		public BooleanPreference(String key, boolean defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public BooleanPreference(String key, boolean defaultValue, long expires, PreferencesStorage type) {
			super(key, defaultValue, expires, type);
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

		public LongPreference(String key, long defaultValue, PreferencesStorage type) {
			super(key, defaultValue, type);
		}

		public LongPreference(String key, long defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public LongPreference(String key, long defaultValue, long expires, PreferencesStorage type) {
			super(key, defaultValue, expires, type);
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

		public FloatPreference(String key, float defaultValue, PreferencesStorage type) {
			super(key, defaultValue, type);
		}

		public FloatPreference(String key, float defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public FloatPreference(String key, float defaultValue, long expires, PreferencesStorage type) {
			super(key, defaultValue, expires, type);
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

		public StringPreference(String key, String defaultValue, PreferencesStorage type) {
			super(key, defaultValue, type);
		}

		public StringPreference(String key, String defaultValue, long expires) {
			super(key, defaultValue, expires);
		}

		public StringPreference(String key, String defaultValue, long expires, PreferencesStorage type) {
			super(key, defaultValue, expires, type);
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

	public final class PreferenceEnum<EnumType extends Enum<EnumType>>
			implements Getter<EnumType> {

		private final StringPreference key;

		private final Class<EnumType> enumType;

		public PreferenceEnum(String key, Class<EnumType> enumType, EnumType defaultValue) {
			this(key, enumType, defaultValue, null);
		}

		public PreferenceEnum(String key, Class<EnumType> enumType, EnumType defaultValue, long expires) {
			this(key, enumType, defaultValue, expires, null);
		}

		public PreferenceEnum(String key, Class<EnumType> enumType, EnumType defaultValue, PreferencesStorage type) {
			this(key, enumType, defaultValue, -1, type);
		}

		@SuppressWarnings("unchecked")
		public PreferenceEnum(String key, Class<EnumType> enumType, EnumType defaultValue, long expires, PreferencesStorage type) {
			this.key = new StringPreference(key, defaultValue == null ? null : defaultValue.name(), expires, type);
			this.enumType = enumType;
		}

		@NonNull
		@SuppressWarnings("ConstantConditions")
		public EnumType get() {
			String value = key.get();
			if (value == null)
				return null;
			return Enum.valueOf(enumType, value);
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

		private final PreferencesSerializer<Object, String> serializer;

		public CustomPreference(PreferencesSerializer<Object, String> serializer, String key, Type type) {
			this(serializer, key, type, (ItemType) null);
		}

		public CustomPreference(PreferencesSerializer<Object, String> serializer, String key, Type type, long expires) {
			this(serializer, key, type, null, expires, null);
		}

		public CustomPreference(PreferencesSerializer<Object, String> serializer, String key, Type type, PreferencesStorage preferencesType) {
			this(serializer, key, type, null, -1, preferencesType);
		}

		public CustomPreference(PreferencesSerializer<Object, String> serializer, String key, Type type, ItemType defaultValue) {
			this(serializer, key, type, defaultValue, null);
		}

		public CustomPreference(PreferencesSerializer<Object, String> serializer, String key, Type type, ItemType defaultValue, long expires) {
			this(serializer, key, type, defaultValue, expires, null);
		}

		public CustomPreference(PreferencesSerializer<Object, String> serializer, String key, Type type, ItemType defaultValue, PreferencesStorage preferencesType) {
			this(serializer, key, type, defaultValue, -1, preferencesType);
		}

		@SuppressWarnings("unchecked")
		public CustomPreference(PreferencesSerializer<Object, String> serializer, String key, Type type, ItemType defaultValue, long expires, PreferencesStorage preferencesType) {
			this.key = new StringPreference(key, null, expires, preferencesType);
			this.defaultValue = defaultValue;
			this.serializer = serializer;
			itemType = type;
		}

		@SuppressWarnings("unchecked")
		public ItemType get() {
			if (cache != null) {
				logDebug("+----+ CACHED: " + key.key + ": " + cache);
				return cache;
			}

			String value = key.get();
			if (value == null) {
				cache = defaultValue;
				logDebug("+----+ DEFAULT: " + key.key + ": " + cache);
				return cache;
			}

			cache = (ItemType) serializer.mapRev(itemType, value);
			logInfo("+----+ DESERIALIZED: " + key.key + ": " + cache);
			return cache;
		}

		public void set(ItemType value) {
			String valueAsString = value == null ? null : serializer.map(value);
			cache = value;
			key.set(valueAsString);
		}

		public void delete() {
			clearExpiration(key);
			removeValue(key);
			cache = null;
		}
	}

	public interface PreferencesSerializer<From, To>
			extends Function<From, To> {

		From mapRev(Type fromType, To from);
	}
}

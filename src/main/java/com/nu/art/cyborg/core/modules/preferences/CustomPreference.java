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

import com.nu.art.core.interfaces.Getter;
import com.nu.art.core.interfaces.Serializer;

import java.lang.reflect.Type;

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
				key.logDebug("+----+ CACHED: " + key.key + ": " + cache);
			return cache;
		}

		String value = key.get(printToLog);
		if (value == null) {
			cache = defaultValue;
			if (printToLog)
				key.logDebug("+----+ DEFAULT: " + key.key + ": " + cache);
			return cache;
		}

		try {
			cache = (ItemType) serializer.deserialize(value, itemType);
		} catch (Exception e) {
			key.logError("Error while deserializing item type: " + itemType + ", probably changed class structure... returning null", e);
			cache = null;
		}

		if (printToLog)
			key.logInfo("+----+ DESERIALIZED: " + key.key + ": " + cache);
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
		key.delete();
		cache = null;
	}
}
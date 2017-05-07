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

package com.nu.art.cyborg.core.more;

import android.os.Bundle;

import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.exceptions.runtime.NotImplementedYetException;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.Restorable;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.more.CyborgStateInjector.TypeParser;
import com.nu.art.reflection.extractor.AnnotatbleExtractor;
import com.nu.art.reflection.tools.ART_Tools;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class CyborgStateExtractor
		extends AnnotatbleExtractor<Restorable, Object, Object> {

	private final Bundle data;

	private final String keyPrefix;

	public CyborgStateExtractor(Bundle data) {
		this(null, data);
	}

	public CyborgStateExtractor(String keyPrefix, Bundle data) {
		super(Restorable.class);
		this.data = data;
		if (keyPrefix == null)
			this.keyPrefix = "";
		else
			this.keyPrefix = keyPrefix + "-";
	}

	@Override
	protected final Field[] extractFieldsFromInstance(Class<?> controllerType) {
		return ART_Tools.getFieldsWithAnnotationAndTypeFromClassHierarchy(controllerType, Object.class, annotationType, annotationType);
	}

	@Override
	protected void setValueFromAnnotationAndField(Restorable annotation, Field field, Object fieldValue) {
		Class<?> fieldType = ReflectiveTools.getBoxedType(field.getType());
		String key = annotation.key();
		if (key == null || key.length() == 0)
			key = keyPrefix + field.getName();

		if (fieldValue == null)
			return;

		if (annotation.parserType() != TypeParser.class)
			serializeWithParser(annotation, fieldType, annotation.parserType(), fieldValue, data, key);
		else if (fieldType.isArray())
			serializeArray(annotation, fieldType.getComponentType(), fieldValue, data, key);
		else if (List.class.isAssignableFrom(fieldType))
			serializeList(annotation, fieldType, fieldValue, data, key);
		else if (Map.class.isAssignableFrom(fieldType))
			serializeMap(annotation, fieldType, fieldValue, data, key);
		else if (File.class.isAssignableFrom(fieldType))
			data.putString(key, ((File) fieldValue).getAbsolutePath());
		else if (Class.class.isAssignableFrom(fieldType))
			data.putString(key, ((Class<?>) fieldValue).getName());
		else if (Enum.class.isAssignableFrom(fieldType))
			data.putString(key, ((Enum<?>) fieldValue).name());
		else if (fieldType == Double.class)
			data.putDouble(key, (Double) fieldValue);
		else if (fieldType == Float.class)
			data.putFloat(key, (Float) fieldValue);
		else if (fieldType == Byte.class)
			data.putByte(key, (Byte) fieldValue);
		else if (fieldType == Character.class)
			data.putChar(key, (Character) fieldValue);
		else if (fieldType == Short.class)
			data.putShort(key, (Short) fieldValue);
		else if (fieldType == Integer.class)
			data.putInt(key, (Integer) fieldValue);
		else if (fieldType == Long.class)
			data.putLong(key, (Long) fieldValue);
		else if (fieldType == Boolean.class)
			data.putBoolean(key, (Boolean) fieldValue);
		else if (fieldType == String.class)
			data.putString(key, (String) fieldValue);
		else
			throw new ImplementationMissingException("Unhandled type: " + fieldType);
	}

	private void serializeMap(Restorable annotation, Class<?> fieldType, Object fieldValue, Bundle data2, String key) {
		if (CyborgBuilder.getInstance().isDebugCertificate())
			throw new NotImplementedYetException("serializeMap");
	}

	@SuppressWarnings("unchecked")
	private <ListNodeType> void serializeList(Restorable annotation, Class<ListNodeType> fieldType, Object fieldValue, Bundle data, String key) {
		if (CyborgBuilder.getInstance().isDebugCertificate())
			throw new NotImplementedYetException("serializeList");

		ListNodeType[] array = ArrayTools.asArray((List<ListNodeType>) fieldValue, fieldType);
		serializeArray(annotation, fieldType, array, data, key);
	}

	private <ArrayType> void serializeArray(Restorable annotation, Class<ArrayType> fieldType, Object fieldValue, Bundle data, String key) {
		if (fieldValue == null)
			return;
		Class<?> arrayType = ReflectiveTools.getBoxedType(fieldType);
		if (arrayType == Double.class)
			data.putDoubleArray(key, (double[]) fieldValue);
		else if (arrayType == Float.class)
			data.putFloatArray(key, (float[]) fieldValue);
		else if (arrayType == Byte.class)
			data.putByteArray(key, (byte[]) fieldValue);
		else if (arrayType == Character.class)
			data.putCharArray(key, (char[]) fieldValue);
		else if (arrayType == Short.class)
			data.putShortArray(key, (short[]) fieldValue);
		else if (arrayType == Integer.class)
			data.putIntArray(key, (int[]) fieldValue);
		else if (arrayType == Long.class)
			data.putLongArray(key, (long[]) fieldValue);
		else if (arrayType == Boolean.class)
			data.putBooleanArray(key, (boolean[]) fieldValue);
		else if (arrayType == String.class)
			data.putStringArray(key, (String[]) fieldValue);
		else if (CyborgBuilder.getInstance().isDebugCertificate())
			throw new NotImplementedYetException("serializeArray");
	}

	private void serializeWithParser(Restorable annotation, Class<?> fieldType, Class<? extends TypeParser> parserType, Object fieldValue, Bundle data, String key) {
		TypeParser parser = ReflectiveTools.newInstance(parserType);
		parser.serialize(annotation, fieldType, fieldValue, data, key);
	}
}

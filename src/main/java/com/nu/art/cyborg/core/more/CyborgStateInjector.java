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

package com.nu.art.cyborg.core.more;

import android.os.Bundle;
import android.os.Debug;

import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.exceptions.runtime.NotImplementedYetException;
import com.nu.art.core.exceptions.runtime.ThisShouldNotHappenedException;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.Restorable;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.reflection.injector.AnnotatbleInjector;
import com.nu.art.reflection.tools.ART_Tools;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class CyborgStateInjector
	extends AnnotatbleInjector<Restorable, Object, Object>
	implements ILogger {

	public interface TypeParser {

		Object deserialize(Restorable annotation, Class<?> fieldType, Bundle data, String key);

		Object serialize(Restorable annotation, Class<?> fieldType, Object fieldValue, Bundle data, String key);
	}

	private final String TAG = getClass().getSimpleName();

	private final Bundle data;

	private final String keyPrefix;

	private ILogger logger;

	public CyborgStateInjector(Bundle data) {
		this(null, data);
		logger = CyborgBuilder.getInstance().getLogger(this);
	}

	public CyborgStateInjector(String keyPrefix, Bundle data) {
		super(Restorable.class);
		this.data = data;
		if (keyPrefix == null)
			this.keyPrefix = "";
		else
			this.keyPrefix = keyPrefix + "-";
	}

	@Override
	protected final Field[] extractFieldsFromInstance(Class<?> injecteeType) {
		return ART_Tools.getFieldsWithAnnotationAndTypeFromClassHierarchy(injecteeType, Object.class, null, annotationType);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object getValueFromAnnotationAndField(Object value, Restorable annotation, Field field) {
		Class<?> fieldType = ReflectiveTools.getBoxedType(field.getType());
		String key = annotation.key();
		if (key.length() == 0)
			key = keyPrefix + field.getName();

		if (!data.containsKey(key))
			return value;

		if (annotation.parserType() != TypeParser.class)
			return deserializeWithParser(annotation, fieldType, annotation.parserType(), data, key);
		if (fieldType.isArray())
			return deserializeArray(fieldType.getComponentType(), data, key);
		if (List.class.isAssignableFrom(fieldType))
			return deserializeList(annotation.listNodeType(), (Class<? extends List>) fieldType, data, key);
		if (Map.class.isAssignableFrom(fieldType))
			return deserializeMap(annotation.mapKeyType(), annotation.mapValueType(), fieldType, data, key);
		if (File.class.isAssignableFrom(fieldType))
			return new File(data.getString(key));
		if (Class.class.isAssignableFrom(fieldType))
			try {
				return Cyborg.class.getClassLoader().loadClass(data.getString(key));
			} catch (ClassNotFoundException e) {
				throw new ThisShouldNotHappenedException(key, e);
			}
		if (Enum.class.isAssignableFrom(fieldType))
			return ReflectiveTools.getEnumFromValue((Class<? extends Enum<?>>) fieldType, data.getString(key));
		if (fieldType == Double.class)
			return data.getDouble(key);
		if (fieldType == Float.class)
			return data.getFloat(key);
		if (fieldType == Byte.class)
			return data.getByte(key);
		if (fieldType == Character.class)
			return data.getChar(key);
		if (fieldType == Short.class)
			return data.getShort(key);
		if (fieldType == Integer.class)
			return data.getInt(key);
		if (fieldType == Long.class)
			return data.getLong(key);
		if (fieldType == Boolean.class)
			return data.getBoolean(key);
		if (fieldType == String.class)
			return data.getString(key);
		throw new ImplementationMissingException("Unhandled type: " + fieldType);
	}

	private Object deserializeMap(Class<?> keyType, Class<?> valueType, Class<?> fieldType, Bundle data, String key) {
		if (CyborgBuilder.getInstance().isDebugCertificate())
			throw new NotImplementedYetException("deserializeMap");
		return null;
	}

	@SuppressWarnings("unchecked")
	private <ListNodeType> Object deserializeList(Class<ListNodeType> nodeType, Class<? extends List> fieldType, Bundle data, String key) {
		List<ListNodeType> list = ReflectiveTools.newInstance(fieldType);
		ListNodeType[] array = (ListNodeType[]) deserializeArray(nodeType, data, key);
		list.addAll(Arrays.asList(array));
		return list;
	}

	private <ArrayType> Object deserializeArray(Class<ArrayType> componentType, Bundle data, String key) {
		Class<?> arrayType = ReflectiveTools.getBoxedType(componentType);
		if (arrayType == Double.class)
			return data.getDoubleArray(key);
		if (arrayType == Float.class)
			return data.getFloatArray(key);
		if (arrayType == Byte.class)
			return data.getByteArray(key);
		if (arrayType == Character.class)
			return data.getCharArray(key);
		if (arrayType == Short.class)
			return data.getShortArray(key);
		if (arrayType == Integer.class)
			return data.getIntArray(key);
		if (arrayType == Long.class)
			return data.getLongArray(key);
		if (arrayType == Boolean.class)
			return data.getBooleanArray(key);
		if (arrayType == String.class)
			return data.getStringArray(key);

		if (CyborgBuilder.getInstance().isDebugCertificate())
			throw new NotImplementedYetException("deserializeArray");

		logWarning("!!! Unable to deserialize array or list fo type: " + componentType.getSimpleName() + " !!!");
		return ArrayTools.newInstance(componentType, 0);
	}

	private Object deserializeWithParser(Restorable annotation, Class<?> fieldType, Class<? extends TypeParser> parserType, Bundle data, String key) {
		TypeParser parser = ReflectiveTools.newInstance(parserType);
		return parser.deserialize(annotation, fieldType, data, key);
	}

	@Override
	public void logVerbose(String verbose) {
		if (logger != null)
			logger.logVerbose(verbose);
	}

	@Override
	public void logVerbose(String verbose, Object... params) {
		if (logger != null)
			logger.logVerbose(verbose, params);
	}

	@Override
	public void logVerbose(Throwable e) {
		if (logger != null)
			logger.logVerbose(e);
	}

	@Override
	public void logVerbose(String verbose, Throwable e) {
		if (logger != null)
			logger.logVerbose(verbose, e);
	}

	@Override
	public void logDebug(String debug) {
		if (logger != null)
			logger.logDebug(debug);
	}

	@Override
	public void logDebug(String debug, Object... params) {
		if (logger != null)
			logger.logDebug(debug, params);
	}

	@Override
	public void logDebug(Throwable e) {
		if (logger != null)
			logger.logDebug(e);
	}

	@Override
	public void logDebug(String debug, Throwable e) {
		if (logger != null)
			logger.logDebug(debug, e);
	}

	@Override
	public void logInfo(String info) {
		if (logger != null)
			logger.logInfo(info);
	}

	@Override
	public void logInfo(String info, Object... params) {
		if (logger != null)
			logger.logInfo(info, params);
	}

	@Override
	public void logInfo(Throwable e) {
		if (logger != null)
			logger.logInfo(e);
	}

	@Override
	public void logInfo(String info, Throwable e) {
		if (logger != null)
			logger.logInfo(info, e);
	}

	@Override
	public void logWarning(String warning) {
		if (logger != null)
			logger.logWarning(warning);
	}

	@Override
	public void logWarning(String warning, Object... params) {
		if (logger != null)
			logger.logWarning(warning, params);
	}

	@Override
	public void logWarning(Throwable e) {
		if (logger != null)
			logger.logWarning(e);
	}

	@Override
	public void logWarning(String warning, Throwable e) {
		if (logger != null)
			logger.logWarning(warning, e);
	}

	@Override
	public void logError(String error) {
		if (logger != null)
			logger.logError(error);
	}

	@Override
	public void logError(String error, Object... params) {
		if (logger != null)
			logger.logError(error, params);
	}

	@Override
	public void logError(Throwable e) {
		if (logger != null)
			logger.logError(e);
	}

	@Override
	public void logError(String error, Throwable e) {
		if (logger != null)
			logger.logError(error, e);
	}
}

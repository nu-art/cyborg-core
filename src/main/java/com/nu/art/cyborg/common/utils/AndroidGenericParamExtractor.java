package com.nu.art.cyborg.common.utils;

import com.nu.art.belog.Logger;
import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.modular.core.EventDispatcher.GenericParamExtractor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class AndroidGenericParamExtractor
	extends Logger
	implements GenericParamExtractor {

	private Field argsField;

	private Field resolvedTypesField;

	public AndroidGenericParamExtractor() {
		try {
			Class<?> parametrizedType = getParameterizedType();
			argsField = parametrizedType.getDeclaredField("args");
			argsField.setAccessible(true);

			Class<?> listOfTypes = getListOfTypes();
			resolvedTypesField = listOfTypes.getDeclaredField("resolvedTypes");
			resolvedTypesField.setAccessible(true);
		} catch (Throwable e) {
			throw new MUST_NeverHappenedException("Error extracting processor generic parameter fields for runtime use", e);
		}
	}

	private Class<?> getListOfTypes()
		throws ClassNotFoundException {
		try {
			return Class.forName("libcore.reflect.ListOfTypes");
		} catch (Throwable ignore) {}

		try {
			return Class.forName("sun.reflect.generics.reflectiveObjects.ListOfTypes");
		} catch (Throwable ignore) {}

		try {
			return Class.forName("org.apache.harmony.luni.lang.reflect.ListOfTypes");
		} catch (Throwable e) {
			throw new MUST_NeverHappenedException("Error extracting processor generic parameter fields for runtime use", e);
		}
	}

	private Class<?> getParameterizedType()
		throws ClassNotFoundException {
		try {
			return Class.forName("libcore.reflect.ParameterizedTypeImpl");
		} catch (Throwable ignore) {}

		try {
			return Class.forName("sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl");
		} catch (Throwable ignore) {}

		try {
			return Class.forName("org.apache.harmony.luni.lang.reflect.ImplForType");
		} catch (Throwable e) {
			throw new MUST_NeverHappenedException("Error extracting processor generic parameter fields for runtime use", e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> extractGenericType(Object o, int index) {
		try {
			Type type = o.getClass().getGenericInterfaces()[0];
			Object list;
			list = argsField.get(type);
			return (Class<T>) Array.get(resolvedTypesField.get(list), 0);
		} catch (Throwable e) {
			throw new MUST_NeverHappenedException("Error extracting processor generic parameter from processor type: " + o.getClass(), e);
		}
	}
}

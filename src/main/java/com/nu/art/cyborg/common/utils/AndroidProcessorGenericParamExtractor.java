package com.nu.art.cyborg.common.utils;

import com.nu.art.belog.Logger;
import com.nu.art.core.exceptions.runtime.MUST_NeverHappenedException;
import com.nu.art.core.generics.Processor;
import com.nu.art.modular.core.EventDispatcher.ProcessorGenericParamExtractor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class AndroidProcessorGenericParamExtractor
		extends Logger
		implements ProcessorGenericParamExtractor {

	private Field argsField;

	private Field resolvedTypesField;

	public AndroidProcessorGenericParamExtractor() {
		try {
			Class<?> parameterizedType = Class.forName("libcore.reflect.ParameterizedTypeImpl");
			argsField = parameterizedType.getDeclaredField("args");
			argsField.setAccessible(true);

			Class<?> listOfTypes = Class.forName("libcore.reflect.ListOfTypes");
			resolvedTypesField = listOfTypes.getDeclaredField("resolvedTypes");
			resolvedTypesField.setAccessible(true);
		} catch (Throwable e) {
			throw new MUST_NeverHappenedException("Error extracting processor generic parameter fields for runtime use", e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> extractGenericTypeFromProcessorTest(Processor<T> processor) {
		try {
			Type type = processor.getClass().getGenericInterfaces()[0];
			Object list;
			list = argsField.get(type);
			return (Class<T>) Array.get(resolvedTypesField.get(list), 0);
		} catch (Throwable e) {
			throw new MUST_NeverHappenedException("Error extracting processor generic parameter from processor type: " + processor.getClass(), e);
		}
	}
}

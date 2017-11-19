package com.nu.art.cyborg.core.consts;

import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Processor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Created by tacb0ss on 14/11/2017.
 */

public class DebugFlags {

	public static boolean DebugPerformance = false;
	public static boolean DebugActivityLifeCycle = false;
	public static boolean DebugControllerLifeCycle = false;
	public static ExtractGenericParamFromProcessor paramExtractor;

	public static class ExtractGenericParamFromProcessor
			extends Logger {

		private Field argsField;

		private Field resolvedTypesField;

		public ExtractGenericParamFromProcessor() {
			try {
				Class<?> parameterizedType = Class.forName("libcore.reflect.ParameterizedTypeImpl");
				argsField = parameterizedType.getDeclaredField("args");
				argsField.setAccessible(true);

				Class<?> listOfTypes = Class.forName("libcore.reflect.ListOfTypes");
				resolvedTypesField = listOfTypes.getDeclaredField("resolvedTypes");
				resolvedTypesField.setAccessible(true);
			} catch (Throwable e) {
				logWarning("Error while setting up the generic params extraction test", e);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> Class<T> extractGenericTypeFromProcessorTest(Class<T> fallback, Processor<T> processor) {
			try {
				Type type = processor.getClass().getGenericInterfaces()[0];
				Object list;
				list = argsField.get(type);
				return (Class<T>) Array.get(resolvedTypesField.get(list), 0);
			} catch (Throwable e) {
				return fallback;
			}
		}
	}
}

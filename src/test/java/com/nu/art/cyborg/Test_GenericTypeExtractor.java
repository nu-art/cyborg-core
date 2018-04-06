package com.nu.art.cyborg;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Function;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.modules.AndroidLogClient;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static com.nu.art.core.generics.GenericParamExtractor._GenericParamExtractor;

/**
 * Created by tacb0ss on 05/04/2018.
 */

public class Test_GenericTypeExtractor
	extends Logger {

	class GenericTestListener<A, B, C> {

	}

	abstract class MiddleListener<X, Y, Z>
		extends GenericTestListener<X, Y, Z> {

		public MiddleListener() {
		}
	}

	class SecondParamListener<J, K>
		extends MiddleListener<K, String, J> {

	}

	class InterfaceTest<D>
		extends InterfaceMiddle<D, Integer>
		implements Processor<D> {

		@Override
		public void process(D d) {

		}
	}

	class InterfaceMiddle<D, K>
		implements Function<K, D> {

		@Override
		public D map(K k) {
			return null;
		}
	}

	private static boolean setUpIsDone = false;

	@Before
	public void setUp() {
		if (setUpIsDone) {
			return;
		}

		BeLogged.getInstance().addClient(new AndroidLogClient());
		setUpIsDone = true;
	}

	@Test
	public void test_HttpListenerGenericType() {
		//		FirstParamListener<InputStream> p = new FirstParamListener<InputStream>() {};
		SecondParamListener<InputStream, BeLogged> p = new SecondParamListener<InputStream, BeLogged>() {};

		Class<?> _class1 = _GenericParamExtractor.extractGenericType(GenericTestListener.class, p, 1);
		logDebug("_class1: " + _class1.getName());

		Class<?> _class0 = _GenericParamExtractor.extractGenericType(GenericTestListener.class, p, 0);
		logDebug("_class0: " + _class0.getName());

		Class<?> _class2 = _GenericParamExtractor.extractGenericType(GenericTestListener.class, p, 2);
		logDebug("_class2: " + _class2.getName());
	}

	@Test
	public void test_ProcessorGenericType() {
		//		FirstParamListener<InputStream> p = new FirstParamListener<InputStream>() {};
		InterfaceTest<Double> p = new InterfaceTest<Double>() {};

		Class<?> ___classProc0 = _GenericParamExtractor.extractGenericType(Processor.class, new Processor<String>() {
			@Override
			public void process(String s) {

			}
		}, 0);
		logDebug("___classProc 0: " + ___classProc0.getName());

		Class<?> __classProc0 = _GenericParamExtractor.extractGenericType(Processor.class, p, 0);
		logDebug("_classProc 0: " + __classProc0.getName());

		Class<?> _classFunc0 = _GenericParamExtractor.extractGenericType(Function.class, p, 0);
		logDebug("_classFunc 0: " + _classFunc0.getName());

		Class<?> _classFunc1 = _GenericParamExtractor.extractGenericType(Function.class, p, 1);
		logDebug("_classFunc 1: " + _classFunc1.getName());
	}
}

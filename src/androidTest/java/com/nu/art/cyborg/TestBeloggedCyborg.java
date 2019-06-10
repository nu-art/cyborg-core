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

package com.nu.art.cyborg;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.Logger;
import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.exceptions.runtime.NotImplementedYetException;
import com.nu.art.cyborg.core.modules.AndroidLogger;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by TacB0sS on 23/04/2018.
 */
public class TestBeloggedCyborg
	extends Logger {

	private static boolean setUpIsDone = false;
	private AndroidLogger logClient;

	@Before
	public void setUp() {
		if (setUpIsDone) {
			return;
		}

		logClient = new AndroidLogger();
		BeLogged.getInstance().addClient(logClient);
		setUpIsDone = true;
	}

	@Test
	public void testBelogged() {
		log(LogLevel.Debug, "Exception with cause", new NotImplementedYetException("Test Exception error", new Exception("cause")));
		log(LogLevel.Debug, "Wrong Param: Incoming%202.mp3", new NotImplementedYetException("Test Exception error"));
		log(LogLevel.Debug, "%s: Testing param", "Test");
		log(LogLevel.Info, "Testing no param");

		logClient.setLogLevel(LogLevel.Warning, LogLevel.Assert);
		log(LogLevel.Info, "Should NOT be shown");
		log(LogLevel.Warning, "Should be shown warning");
		log(LogLevel.Error, "Should be shown error");
		log(LogLevel.Error, "Should be shown With exception", new NotImplementedYetException("Test Exception error"));

		logClient.setLogLevel(LogLevel.Verbose, LogLevel.Warning);
		log(LogLevel.Error, "Should NOT be shown error");
		log(LogLevel.Debug, "Should be shown With exception", new NotImplementedYetException("Test Exception debug"));
		log(LogLevel.Debug, "Should be shown With exception %s", new NotImplementedYetException("Test Exception debug"));
		log(LogLevel.Debug, "Should be shown With param and exception %s and %s", "Donno", new NotImplementedYetException("Test Exception debug"));

		log(LogLevel.Info, new NotImplementedYetException("Exception only"));
		BeLogged.getInstance().setLogLevel(LogLevel.Warning, LogLevel.Assert);
		log(LogLevel.Info, "Should NOT be shown");
		log(LogLevel.Warning, "testing log with %F in it");
	}
}

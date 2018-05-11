package com.nu.art.cyborg;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.Logger;
import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.exceptions.runtime.NotImplementedYetException;
import com.nu.art.cyborg.core.modules.AndroidLogClient;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by tacb0ss on 23/04/2018.
 */
public class TestBeloggedCyborg
	extends Logger {

	private static boolean setUpIsDone = false;
	private AndroidLogClient logClient;

	@Before
	public void setUp() {
		if (setUpIsDone) {
			return;
		}

		logClient = new AndroidLogClient();
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

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

package com.nu.art.cyborg.core.modules.crashReport;

import android.os.Build;
import android.os.StrictMode;

import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ExceptionTools;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.PreferencesModule;
import com.nu.art.cyborg.core.modules.PreferencesModule.BooleanPreference;

import java.io.File;
import java.io.FileInputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by TacB0sS on 19-Sep 2016.
 */
public class CrashReportModule
		extends CyborgModule
		implements UncaughtExceptionHandler, CrashReportListener {

	private CrashReportHandler crashReportHandler;

	private UncaughtExceptionHandler defaultExceptionHandler;

	private BooleanPreference sendDebugCrashReports;

	private BooleanPreference hasCrashReportWaiting;

	/**
	 * Will only be available while a crash report composing is in progress...
	 */
	private CrashReport crashReport;

	public CrashReport getCrashReport() {
		return crashReport;
	}

	@Override
	public void onApplicationCrashed(HashMap<String, Object> moduleCrashData) {
		moduleCrashData.put("Package", getApplicationContext().getPackageName());

		moduleCrashData.put("SERIAL", Build.SERIAL);
		moduleCrashData.put("MODEL", Build.MODEL);
		moduleCrashData.put("ID", Build.ID);
		moduleCrashData.put("MANUFACTURE", Build.MANUFACTURER);
		moduleCrashData.put("BRAND", Build.BRAND);
		moduleCrashData.put("TYPE", Build.TYPE);
		moduleCrashData.put("USER", Build.USER);
		moduleCrashData.put("BASE", Build.VERSION_CODES.BASE);
		moduleCrashData.put("INCREMENTAL", Build.VERSION.INCREMENTAL);
		moduleCrashData.put("SDK", Build.VERSION.SDK);
		moduleCrashData.put("BOARD", Build.BOARD);
		moduleCrashData.put("BRAND", Build.BRAND);
		moduleCrashData.put("HOST", Build.HOST);
		moduleCrashData.put("FINGERPRINT", Build.FINGERPRINT);
		moduleCrashData.put("VERSION CODE", Build.VERSION.RELEASE);
	}

	public void setForceDebugCrashReport(boolean reportInDebug) {
		sendDebugCrashReports.set(reportInDebug);
	}

	public void setCrashReportHandler(CrashReportHandler crashReportHandler) {
		this.crashReportHandler = crashReportHandler;
	}

	public void setDefaultExceptionHandler(UncaughtExceptionHandler defaultExceptionHandler) {
		this.defaultExceptionHandler = defaultExceptionHandler;
	}

	@Override
	protected void init() {
		if (crashReportHandler == null)
			throw new ImplementationMissingException("MUST provide this module with a crash report handler in your module pack!");

		sendDebugCrashReports = getModule(PreferencesModule.class).new BooleanPreference("sendDebugCrashReports", false);
		hasCrashReportWaiting = getModule(PreferencesModule.class).new BooleanPreference("hasCrashReportWaiting", false);

		if (defaultExceptionHandler == null)
			defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(this);
		if (isDebug())
			forceStrictPolicy();
	}

	private void forceStrictPolicy() {
		// Enable all thread strict mode policies
		StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog();

		// Enable all VM strict mode policies
		StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();

		// Use builders to enable strict mode policies
		StrictMode.setThreadPolicy(threadPolicyBuilder.build());
		StrictMode.setVmPolicy(vmPolicyBuilder.build());
	}

	@Override
	protected void printModuleDetails() {
		super.printModuleDetails();
		logDebug("hasCrashReportWaiting: " + hasCrashReportWaiting.get());
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		logError("Crash on thread: " + thread.getName(), ex);
		logError(" ");
		logError(" _______  _______  _______  _______           _______  ______  ");
		logError("(  ____ \\(  ____ )(  ___  )(  ____ \\|\\     /|(  ____ \\(  __  \\ ");
		logError("| (    \\/| (    )|| (   ) || (    \\/| )   ( || (    \\/| (  \\  )");
		logError("| |      | (____)|| (___) || (_____ | (___) || (__    | |   ) |");
		logError("| |      |     __)|  ___  |(_____  )|  ___  ||  __)   | |   | |");
		logError("| |      | (\\ (   | (   ) |      ) || (   ) || (      | |   ) |");
		logError("| (____/\\| ) \\ \\__| )   ( |/\\____) || )   ( || (____/\\| (__/  )");
		logError("(_______/|/   \\__/|/     \\|\\_______)|/     \\|(_______/(______/ ");
		logError(" ");
		if (!isDebug() || sendDebugCrashReports.get())
			composeAndSendReport(thread, ex, true);

		defaultExceptionHandler.uncaughtException(thread, ex);
	}

	public void composeAndSendReport() {
		composeAndSendReport(null, null, false);
	}

	public void composeAndSendReport(String uuid) {
		composeAndSendReport(uuid, null, null, false);
	}

	public void composeAndSendReport(Throwable ex) {
		composeAndSendReport(Thread.currentThread(), ex, false);
	}

	private void composeAndSendReport(Thread thread, Throwable ex, boolean crashed) {
		composeAndSendReport(null, thread, ex, crashed);
	}

	private void composeAndSendReport(String uuid, Thread thread, Throwable ex, boolean crashed) {
		try {
			crashReport = new CrashReport(uuid);
			crashReport.crashMessage = composeMessage(thread, ex, crashed);
			crashReport.modulesData = collectModulesData();
			crashReport.runningThreads = getRunningThreads();
			crashReport.threadTraces = getTraces();

			crashReportHandler.prepareAndBackupCrashReport(crashReport);
			hasCrashReportWaiting.set(true);

			crashReportHandler.sendCrashReport(crashReport, new Processor<Throwable>() {
				@Override
				public void process(Throwable throwable) {
					if (throwable != null)
						hasCrashReportWaiting.set(false);

					try {
						crashReportHandler.deleteBackup();
					} catch (Exception e) {
						logError("Error deleting crash report: ", e);
					}
				}
			});
		} catch (Throwable e) {
			logError("Error sending crash report: ", e);
		}
	}

	private String getTraces() {
		File tracesFile = new File("/data/anr/traces.txt");
		if (!tracesFile.exists())
			return "";

		try {
			return StreamTools.readFullyAsString(new FileInputStream(tracesFile));
		} catch (Exception e) {
			return "Error Getting traces: " + ExceptionTools.getStackTrace(e);
		}
	}

	private HashMap<String, ThreadState> getRunningThreads() {
		HashMap<String, ThreadState> threads = new HashMap<>();

		Map<Thread, StackTraceElement[]> systemThreads = Thread.getAllStackTraces();
		for (Thread thread : systemThreads.keySet()) {
			ThreadState state = new ThreadState();
			threads.put(thread.getName(), state);
			state.alive = thread.isAlive();
			state.daemon = thread.isDaemon();
			state.interrupted = thread.isInterrupted();
			state.id = thread.getId();
			state.priority = thread.getPriority();
			state.threadGroup = thread.getThreadGroup().getName();
			state.state = thread.getState().name();
			state.stacktrace = ExceptionTools.parseStackTrace(thread.getStackTrace());
		}
		return threads;
	}

	//TODO Convert this idiotic API to builder, a report builder!!
	//	public final class ReportBuilder {
	//
	//		String uuid;
	//
	//		Throwable t;
	//
	//		boolean crashed;
	//	}
	//
	//	public ReportBuilder composeReport() {
	//		return new ReportBuilder();
	//	}

	private String composeMessage(Thread thread, Throwable ex, boolean crash) {
		StringBuilder crashReport = new StringBuilder();

		if (crash)
			crashReport.append("Application Crashed!");
		else if (ex != null)
			crashReport.append("Exception report");

		if (thread != null)
			crashReport.append("Thread: ").append(thread.getName()).append("\n");
		else
			crashReport.append("User sent a bug report");

		if (ex != null)
			crashReport.append(ExceptionTools.getStackTrace(ex));

		return crashReport.toString();
	}

	private HashMap<String, HashMap<String, Object>> collectModulesData() {
		CrashReportListener[] listeners = getModulesAssignableFrom(CrashReportListener.class);
		HashMap<String, HashMap<String, Object>> modulesData = new HashMap<>();

		for (CrashReportListener listener : listeners) {
			HashMap<String, Object> moduleCrashData = new HashMap<>();
			modulesData.put(listener.getClass().getSimpleName(), moduleCrashData);
			try {
				listener.onApplicationCrashed(moduleCrashData);
			} catch (Exception e) {
				moduleCrashData.put("Error", ExceptionTools.getStackTrace(e));
			}
		}
		return modulesData;
	}

	public interface CrashReportHandler {

		void prepareAndBackupCrashReport(CrashReport crashReport)
				throws Exception;

		void sendCrashReport(CrashReport crashReport, Processor<Throwable> resultListener)
				throws Exception;

		void deleteBackup()
				throws Exception;
	}
}

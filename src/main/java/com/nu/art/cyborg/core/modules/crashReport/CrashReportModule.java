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

package com.nu.art.cyborg.core.modules.crashReport;

import android.os.Build;
import android.os.StrictMode;

import com.nu.art.core.archiver.ArchiveWriter;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.Serializer;
import com.nu.art.core.tools.ExceptionTools;
import com.nu.art.core.tools.FileTools;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.PreferencesModule;
import com.nu.art.cyborg.core.modules.PreferencesModule.BooleanPreference;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by TacB0sS on 19-Sep 2016.
 */
public class CrashReportModule
	extends CyborgModule
	implements UncaughtExceptionHandler, ModuleStateCollector {

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
	public void collectModuleState(HashMap<String, Object> moduleCrashData) {
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

	public void setDefaultExceptionHandler(UncaughtExceptionHandler defaultExceptionHandler) {
		this.defaultExceptionHandler = defaultExceptionHandler;
	}

	@Override
	protected void init() {
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
			dispatchModuleEvent("Application crashed", OnApplicationCrashed.class, new Processor<OnApplicationCrashed>() {
				@Override
				public void process(OnApplicationCrashed listener) {
					listener.onApplicationCrashed();
				}
			});

		defaultExceptionHandler.uncaughtException(thread, ex);
	}

	public NewCrashReport createCrashReport(File reportDir, String fileName) {
		return new NewCrashReport(reportDir, fileName);
	}

	public class NewCrashReport {

		private final File reportDir;
		public final File reportOutputName;
		private HashMap<String, ArrayList<File>> files = new HashMap<>();

		public NewCrashReport(File reportDir, String fileName) {
			this.reportDir = reportDir;
			this.reportOutputName = new File(reportDir, fileName);
		}

		public void logModuleState(String group, String outputFileName, Serializer<Object, String> serializer)
			throws IOException {
			String data = serializer.serialize(collectModulesData());
			addFileToGroup(group, saveToFile(outputFileName, data));
		}

		public final void addFileToGroup(String group, File... files) {
			ArrayList<File> _files = this.files.get(group);
			if (_files == null)
				this.files.put(group, _files = new ArrayList<>());

			_files.addAll(Arrays.asList(files));
		}

		public void logRunningThreads(String group, String outputFileName, Serializer<Object, String> serializer)
			throws IOException {
			String data = serializer.serialize(getRunningThreads());
			addFileToGroup(group, saveToFile(outputFileName, data));
		}

		public void logTracesANR(String group, String outputFileName)
			throws IOException {
			File tracesFile = new File("/data/anr/traces.txt");
			if (!tracesFile.exists())
				return;

			File file = new File(reportDir, outputFileName);
			FileTools.copyFile(tracesFile, file);
			addFileToGroup(group, file);
		}

		private File saveToFile(String outputFileName, String data)
			throws IOException {
			File file = new File(reportDir, outputFileName);
			FileTools.delete(file);
			FileTools.createNewFile(file);
			StreamTools.copy(new ByteArrayInputStream(data.getBytes()), file);
			return file;
		}

		public void archive()
			throws IOException {
			ArchiveWriter archiveWriter = new ArchiveWriter().open(reportOutputName);
			for (String group : files.keySet()) {
				archiveWriter.addFiles(group, files.get(group).toArray(new File[0]));
			}
			archiveWriter.close();
		}
	}

	//	public void composeAndSendReport() {
	//		composeAndSendReport(null, null, false);
	//	}
	//
	//	public void composeAndSendReport(String fileName) {
	//		composeAndSendReport(fileName, null, null, false);
	//	}
	//
	//	public void composeAndSendReport(Throwable ex) {
	//		composeAndSendReport(Thread.currentThread(), ex, false);
	//	}
	//
	//	private void composeAndSendReport(Thread thread, Throwable ex, boolean crashed) {
	//		composeAndSendReport(null, thread, ex, crashed);
	//	}
	//
	//	private void composeAndSendReport(String uuid, Thread thread, Throwable ex, boolean crashed) {
	//		try {
	//			logDebug("Composing bug report");
	//			crashReport = new CrashReport(uuid);
	//
	//			try {
	//				crashReport.crashMessage = composeMessage(thread, ex, crashed);
	//			} catch (Exception e) {
	//				logError("Error composing crashMessage: ", e);
	//			}
	//
	//			try {
	//				crashReport.modulesData = collectModulesData();
	//			} catch (Exception e) {
	//				logError("Error collecting modulesData: ", e);
	//			}
	//
	//			try {
	//				crashReport.runningThreads = getRunningThreads();
	//			} catch (Exception e) {
	//				logError("Error mapping running threads: ", e);
	//			}
	//
	//			try {
	//				crashReport.threadTraces = getTraces();
	//			} catch (Exception e) {
	//				logError("Error fetching traces.txt: ", e);
	//			}
	//
	//			logDebug("Composed bug report");
	//			crashReportHandler.prepareAndBackupCrashReport(crashReport);
	//			hasCrashReportWaiting.set(true);
	//
	//			crashReportHandler.sendCrashReport(crashReport, new Processor<Throwable>() {
	//				@Override
	//				public void process(Throwable throwable) {
	//					if (throwable != null)
	//						hasCrashReportWaiting.set(false);
	//
	//					try {
	//						crashReportHandler.deleteBackup();
	//					} catch (Exception e) {
	//						logError("Error deleting crash report: ", e);
	//					}
	//				}
	//			});
	//		} catch (Throwable e) {
	//			logError("Error sending crash report: ", e);
	//		}
	//	}
	//
	//	private String getTraces() {
	//		File tracesFile = new File("/data/anr/traces.txt");
	//		if (!tracesFile.exists())
	//			return "";
	//
	//		try {
	//			return StreamTools.readFullyAsString(new FileInputStream(tracesFile));
	//		} catch (Exception e) {
	//			return "Error Getting traces: " + ExceptionTools.getStackTrace(e);
	//		}
	//	}

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
			ThreadGroup threadGroup = thread.getThreadGroup();
			if (threadGroup != null)
				state.threadGroup = threadGroup.getName();
			state.state = thread.getState().name();
			state.stacktrace = ExceptionTools.parseStackTrace(thread.getStackTrace());
		}
		return threads;
	}

	//TODO Convert this idiotic API to builder, a report builder!!
	//	public final class ReportBuilder {
	//
	//		String fileName;
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
		ModuleStateCollector[] listeners = getModulesAssignableFrom(ModuleStateCollector.class);
		HashMap<String, HashMap<String, Object>> modulesData = new HashMap<>();

		for (ModuleStateCollector listener : listeners) {
			HashMap<String, Object> moduleCrashData = new HashMap<>();
			modulesData.put(listener.getClass().getSimpleName(), moduleCrashData);
			try {
				listener.collectModuleState(moduleCrashData);
			} catch (Exception e) {
				moduleCrashData.put("Error", ExceptionTools.getStackTrace(e));
			}
		}
		return modulesData;
	}

	public interface OnApplicationCrashed {

		void onApplicationCrashed();
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

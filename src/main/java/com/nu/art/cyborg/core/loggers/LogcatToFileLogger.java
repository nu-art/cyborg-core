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

package com.nu.art.cyborg.core.loggers;

import android.os.Environment;
import android.util.Log;

import com.nu.art.belog.BeConfig;
import com.nu.art.belog.BeConfig.LoggerConfig;
import com.nu.art.belog.BeConfig.Rule;
import com.nu.art.belog.LoggerClient;
import com.nu.art.belog.LoggerDescriptor;
import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.tools.FileTools;
import com.nu.art.core.tools.SizeTools;
import com.nu.art.core.tools.StreamTools;
import com.nu.art.cyborg.core.loggers.LogcatToFileLogger.Config_LogcatLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by TacB0sS on 28-Feb 2017.
 */
public class LogcatToFileLogger
	extends LoggerClient<Config_LogcatLogger>
	implements Runnable {

	public static final Rule Rule_AllToLogcatLogger = new Rule().setLoggerKeys(Config_LogcatLogger.KEY);
	public static final LoggerConfig LogConfig_LogcatLogger = new Config_LogcatLogger().setKey(Config_LogcatLogger.KEY);
	public static final BeConfig Config_FastLogcatLogger = new BeConfig().setRules(Rule_AllToLogcatLogger).setLoggersConfig(LogConfig_LogcatLogger);

	private volatile boolean active;
	private Thread thread;

	@Override
	protected void log(LogLevel level, Thread thread, String tag, String message, Throwable t) {}

	@Override
	protected void init() {
		active = true;
		thread = new Thread(this, "logcat-to-file");
		thread.start();
	}

	@Override
	public void run() {
		if (!active) {
			thread = null;
			return;
		}

		if (!MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			dispose();
			return;
		}

		Process process = null;
		try {
			FileTools.mkDir(config.folder);
			File logFile = new File(config.folder, config.fileName);
			process = Runtime.getRuntime().exec("logcat -f " + logFile + " -r " + config.size + " -n " + config.count);
			process.waitFor();
		} catch (Throwable e) {
			System.err.println("Error while collecting logs");
			e.printStackTrace();
			if (process != null) {
				try {
					InputStream errorStream = process.getErrorStream();
					if (errorStream != null) {
						String errorStreamAsString = StreamTools.readFullyAsString(errorStream);
						System.out.println("logcat to file error stream: " + errorStreamAsString);
					}
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			if (process != null)
				process.destroy();
		}

		try {
			Thread.sleep(30000);
		} catch (InterruptedException ignore) {}
		run();
	}

	@Override
	protected void dispose() {
		active = false;
		if (thread != null)
			thread.interrupt();
	}

	public File[] getAllLogFiles() {
		return new File(config.folder).listFiles();
	}

	public static class LogcatLoggerDescriptor
		extends LoggerDescriptor<Config_LogcatLogger, LogcatToFileLogger> {

		public LogcatLoggerDescriptor() {
			super(Config_LogcatLogger.KEY, Config_LogcatLogger.class, LogcatToFileLogger.class);
		}
	}

	public static class Config_LogcatLogger
		extends LoggerConfig {

		public static final String KEY = LogcatToFileLogger.class.getSimpleName();

		String fileName = "logcat.txt";
		String folder;
		long size = 5 * SizeTools.MegaByte;
		int count = 10;

		public Config_LogcatLogger() {
			super(KEY);
		}

		public Config_LogcatLogger setFolder(String folder) {
			this.folder = folder;
			return this;
		}

		public Config_LogcatLogger setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		public Config_LogcatLogger setCount(int count) {
			this.count = count;
			return this;
		}

		public Config_LogcatLogger setSize(long size) {
			this.size = size;
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Config_LogcatLogger that = (Config_LogcatLogger) o;

			if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null)
				return false;
			return folder != null ? folder.equals(that.folder) : that.folder == null;
		}

		@Override
		public int hashCode() {
			int result = fileName != null ? fileName.hashCode() : 0;
			result = 31 * result + (folder != null ? folder.hashCode() : 0);
			return result;
		}

		@SuppressWarnings("MethodDoesntCallSuperMethod")
		public Config_LogcatLogger clone() {
			return new Config_LogcatLogger().setFileName(fileName).setFolder(folder).setCount(count).setSize(size);
		}
	}
}

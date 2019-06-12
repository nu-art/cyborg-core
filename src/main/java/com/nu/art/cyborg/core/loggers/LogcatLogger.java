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
import com.nu.art.cyborg.core.loggers.LogcatLogger.Config_LogcatLogger;

import java.io.File;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by TacB0sS on 28-Feb 2017.
 */
public class LogcatLogger
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
		thread = new Thread(this, "logcat-beacon");
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
		} catch (Exception e) {
			Log.e("Logcat", "Error while collecting logs", e);
			if (process != null)
				process.destroy();
		}

		run();
	}

	@Override
	protected void dispose() {
		active = false;
		if (thread != null)
			thread.interrupt();
	}

	public static class LogcatLoggerDescriptor
		extends LoggerDescriptor<Config_LogcatLogger, LogcatLogger> {

		public LogcatLoggerDescriptor() {
			super(Config_LogcatLogger.KEY, Config_LogcatLogger.class, LogcatLogger.class);
		}
	}

	public static class Config_LogcatLogger
		extends LoggerConfig {

		public static final String KEY = LogcatLogger.class.getSimpleName();

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

		@SuppressWarnings("MethodDoesntCallSuperMethod")
		public Config_LogcatLogger clone() {
			return new Config_LogcatLogger().setFileName(fileName).setFolder(folder).setCount(count).setSize(size);
		}
	}
}

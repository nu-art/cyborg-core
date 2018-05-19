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

package com.nu.art.cyborg.core.modules;

import com.nu.art.belog.BeLoggedClient;
import com.nu.art.belog.consts.LogLevel;
import com.nu.art.core.interfaces.Getter;
import com.nu.art.core.tools.ExceptionTools;
import com.nu.art.core.utils.SynchronizedObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by TacB0sS on 28-Feb 2017.
 */
public class AndroidLogClient
	extends BeLoggedClient {

	private static final String StacktraceIndent = "    ";
	private SynchronizedObject<StringBuffer> syncBuffer = new SynchronizedObject<>(new Getter<StringBuffer>() {
		@Override
		public StringBuffer get() {
			return new StringBuffer();
		}
	});
	private SynchronizedObject<HashSet<StackTraceElement>> syncTracesSet = new SynchronizedObject<>(new Getter<HashSet<StackTraceElement>>() {
		@Override
		public HashSet<StackTraceElement> get() {
			return new HashSet<>();
		}
	});

	@Override
	protected void log(LogLevel level, String thread, String tag, String message, Throwable t) {
		if (!isLoggable(level))
			return;

		StringBuffer buffer = syncBuffer.get();
		buffer.setLength(0);
		buffer.append(thread).append("/").append(tag);

		String tagWithThread = buffer.toString();

		printLog(level, tagWithThread, message);

		boolean isCause = false;
		if (t != null)
			logException(level, t, tagWithThread);
	}

	private void logException(LogLevel level, Throwable t, String tagWithThread) {
		boolean isCause = false;
		Set<StackTraceElement> traces = syncTracesSet.get();
		traces.clear();

		while (t != null) {
			String exceptionMessage = t.getMessage() != null ? ": " + t.getMessage() : "";
			//noinspection StringEquality
			String causedBy = isCause ? "CAUSED BY: " : "";

			printLog(level, tagWithThread, causedBy + t.getClass().getName() + exceptionMessage);
			StackTraceElement[] stackTrace = t.getStackTrace();

			for (int i = 0; i < stackTrace.length; i++) {
				StackTraceElement stackTraceElement = stackTrace[i];
				printLog(level, tagWithThread, StacktraceIndent + ExceptionTools.parseStackTrace(stackTraceElement));

				if (traces.contains(stackTraceElement)) {
					printLog(level, tagWithThread, StacktraceIndent + "... " + (stackTrace.length - i) + " more duplicate traces ");
					break;
				}

				traces.add(stackTraceElement);
			}

			isCause = true;
			t = t.getCause();
		}
	}

	private void printLog(LogLevel level, String tagWithThread, String message) {
		if (message == null)
			return;

		switch (level) {
			case Assert:
			case Error:
				android.util.Log.e(tagWithThread, message);
				break;
			case Warning:
				android.util.Log.w(tagWithThread, message);
				break;
			case Info:
				android.util.Log.i(tagWithThread, message);
				break;
			case Debug:
				android.util.Log.d(tagWithThread, message);
				break;
			case Verbose:
				android.util.Log.v(tagWithThread, message);
				break;
		}
	}
}

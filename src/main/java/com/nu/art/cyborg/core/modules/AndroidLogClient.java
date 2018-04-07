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
import com.nu.art.core.tools.ExceptionTools;

/**
 * Created by TacB0sS on 28-Feb 2017.
 */
public class AndroidLogClient
	extends BeLoggedClient {

	private final StringBuffer buffer = new StringBuffer();

	@Override
	protected void log(final LogLevel level, final String thread, final String tag, final String message, final Throwable t) {
		if (!isLoggable(level))
			return;

		String tagWithThread;
		synchronized (buffer) {
			buffer.append(thread).append("/").append(tag);
			tagWithThread = buffer.toString();
			buffer.setLength(0);
		}

		printLog(level, tagWithThread, message);

		if (t != null) {
			printLog(level, tagWithThread, t.getMessage());
			printLog(level, tagWithThread, ExceptionTools.getStackTrace(t));
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

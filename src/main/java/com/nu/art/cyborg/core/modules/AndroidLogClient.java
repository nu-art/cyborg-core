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

package com.nu.art.cyborg.core.modules;

import com.nu.art.belog.BeLogged.LogEntry;
import com.nu.art.belog.BeLoggedClient;

/**
 * Created by TacB0sS on 28-Feb 2017.
 */
public class AndroidLogClient
		extends BeLoggedClient {

	private static final StringBuffer buffer = new StringBuffer();

	@Override
	protected void log(LogEntry entry, String logEntry) {
		String tagWithThread;
		synchronized (buffer) {
			buffer.append(entry.thread).append("/").append(entry.tag);
			tagWithThread = buffer.toString();
			buffer.setLength(0);
		}

		if (entry.message != null)
			switch (entry.level) {
				case Assert:
				case Error:
					android.util.Log.e(tagWithThread, entry.message);
					break;
				case Warning:
					android.util.Log.w(tagWithThread, entry.message);
					break;
				case Info:
					android.util.Log.i(tagWithThread, entry.message);
					break;
				case Debug:
					android.util.Log.d(tagWithThread, entry.message);
					break;
				case Verbose:
					android.util.Log.v(tagWithThread, entry.message);
					break;
			}

		if (entry.t != null)
			switch (entry.level) {
				case Assert:
				case Error:
					android.util.Log.e(tagWithThread, "", entry.t);
					break;
				case Warning:
					android.util.Log.w(tagWithThread, "", entry.t);
					break;
				case Info:
					android.util.Log.i(tagWithThread, "", entry.t);
					break;
				case Debug:
					android.util.Log.d(tagWithThread, "", entry.t);
					break;
				case Verbose:
					android.util.Log.v(tagWithThread, "", entry.t);
					break;
			}
	}
}

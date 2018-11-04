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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings( {
	                   "WeakerAccess",
	                   "unused"
                   })
public final class CrashReport {

	public final static SimpleDateFormat DefaultTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public String fileName;

	public final long timestampLong = System.currentTimeMillis();

	public final String timestamp = DefaultTimeFormat.format(new Date());

	public String crashMessage;

	public HashMap<String, HashMap<String, Object>> modulesData;

	public HashMap<String, ThreadState> runningThreads;

	public String threadTraces;

	public CrashReport(String fileName) {
		this.fileName = fileName == null ? UUID.randomUUID().toString() : fileName;
	}

	public CrashReport() {
		this(null);
	}
}
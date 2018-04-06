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

import java.util.HashMap;

/**
 * Any module that would want to append data to the crash report, would have to implement this interface.
 *
 * @author TacB0sS
 */
public interface CrashReportListener {

	/**
	 * Upon application crash, this method would be called, allowing you to add content to the crash report.<br>
	 * all you have to do is simply add another key to the map with the data you want to add to the crash report.
	 *
	 * @param moduleCrashData The specific crash report data for the module.
	 */
	void onApplicationCrashed(HashMap<String, Object> moduleCrashData)
		throws Exception;
}
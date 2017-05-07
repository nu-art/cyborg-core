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

package com.nu.art.cyborg.core.interfaces;

import java.io.File;

/**
 * Any module that would want to append data to the crash report, would have to implement this interface.
 *
 * @author TacB0sS
 */
public interface ApplicationCrashListener {

	/**
	 * Upon application crash, this method would be called, allowing you to add content to the crash report.<br>
	 * all you have to do is simply add another file to the crash report folder, and it would be shipped with the
	 * report.
	 *
	 * @param crashReportFolder The specific crash report folder, where all the crash details are aggregated into.
	 */
	void onApplicationCrashed(File crashReportFolder)
			throws Throwable;
}

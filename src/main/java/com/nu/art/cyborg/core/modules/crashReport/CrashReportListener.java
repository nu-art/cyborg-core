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
		 * all you have to do is simply add another file to the crash report folder, and it would be shipped with the
		 * report.
		 *
		 * @param moduleCrashData The specific crash report data for the module.
		 */
		void onApplicationCrashed(HashMap<String, Object> moduleCrashData);
	}
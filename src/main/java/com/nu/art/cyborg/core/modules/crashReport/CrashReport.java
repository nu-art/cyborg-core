package com.nu.art.cyborg.core.modules.crashReport;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings( {
											 "WeakerAccess",
											 "unused"
									 })
public final class CrashReport {

	private final String id = UUID.randomUUID().toString();

	private final long timestamp = System.currentTimeMillis();

	public String crashMessage;

	public HashMap<CrashReportListener, HashMap<String, Object>> modulesData;

	public Map<Thread, StackTraceElement[]> runningThreads;
}
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

	public final String uuid = UUID.randomUUID().toString();

	public final String timestamp = DefaultTimeFormat.format(new Date());

	public String crashMessage;

	public HashMap<CrashReportListener, HashMap<String, Object>> modulesData;

	public HashMap<String, ThreadState> runningThreads;
}
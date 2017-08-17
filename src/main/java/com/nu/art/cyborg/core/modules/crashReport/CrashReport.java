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

	public String uuid;

	public final long timestampLong = System.currentTimeMillis();

	public final String timestamp = DefaultTimeFormat.format(new Date());

	public String crashMessage;

	public HashMap<CrashReportListener, HashMap<String, Object>> modulesData;

	public HashMap<String, ThreadState> runningThreads;

	public String threadTraces;

	public CrashReport(String uuid) {
		this.uuid = uuid == null ? UUID.randomUUID().toString() : uuid;
	}

	public CrashReport() {
		this(UUID.randomUUID().toString());
	}
}
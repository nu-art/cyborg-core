package com.nu.art.cyborg.core.consts;

import java.util.HashSet;

public class DebugFlags {

	private static final HashSet<String> debugFlags = new HashSet<>();

	@Deprecated
	public static boolean DebugPerformance = false;
	@Deprecated
	public static boolean DebugStack = false;
	@Deprecated
	public static boolean DebugActivityLifeCycle = false;
	@Deprecated
	public static boolean DebugControllerLifeCycle = false;

	public static void addDebugFlag(String flag) {
		debugFlags.add(flag);
	}

	public static void removeDebugFlag(String flag) {
		debugFlags.remove(flag);
	}

	public static boolean isDebuggableFlag(String flag) {
		return debugFlags.contains(flag);
	}
}

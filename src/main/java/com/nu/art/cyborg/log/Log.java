///*
// * Copyright (c) 2017 to Adam van der Kruk (Zehavi) AKA TacB0sS - Nu-Art Software
// *
// * This software code is not an 'Open Source'!
// * In order to use this code you MUST have a proper license.
// * In order to obtain a licence please contact me directly.
// *
// * Email: Adam.Zehavi@Nu-Art-Software.com
// */
//
//package com.nu.art.cyborg.log;
//
///**
// * An Android Log wrapper, which allows with a bit of configuration to append default Tag prefix, and manage a debug or
// * release management to indicate whether or not to print out the log to the console.
// *
// * @author TacB0sS
// */
//public final class Log {
//
//	public enum LogLevel {
//		Verbose,
//		Debug,
//		Info,
//		Warning,
//		Error,
//		Assert
//	}
//
//	public static void setShowLogs(boolean showingLogs) {
//		DebugMode = showingLogs;
//	}
//
//	private static boolean DebugMode = true;
//
//	private static final StringBuffer buffer = new StringBuffer();
//
//	private synchronized static String getPreTag(String tag) {
//		buffer.append(Thread.currentThread().getName()).append("/").append(tag);
//		tag = buffer.toString();
//		buffer.setLength(0);
//		return tag;
//	}
//
//	/**
//	 * @param tag   - the tag name usually the class that we came from
//	 * @param debug - the debug message to send to the logcat
//	 */
//	public static void d(String tag, String debug) {
//		log(LogLevel.Debug, tag, debug);
//	}
//
//	/**
//	 * @param tag   - the tag name usually the class that we came from
//	 * @param error - the error message to send to the logcat
//	 */
//	public static void e(String tag, String error) {
//		log(LogLevel.Error, tag, error);
//	}
//
//	/**
//	 * @param tag   - the tag name usually the class that we came from
//	 * @param error - the error message to send to the logcat
//	 */
//	public static void e(String tag, String error, Throwable e) {
//		log(LogLevel.Error, tag, error, e);
//	}
//
//	/**
//	 * @param tag   - the tag name usually the class that we came from
//	 * @param error - the error message to send to the logcat
//	 */
//	public static void e(String tag, Throwable e) {
//		log(LogLevel.Error, tag, e);
//	}
//
//	/**
//	 * @param tag  - the tag name usually the class that we came from
//	 * @param info - the info message to send to the logcat
//	 */
//	public static void i(String tag, String info) {
//		log(LogLevel.Info, tag, info);
//	}
//
//	/**
//	 * @param tag     - the tag name usually the class that we came from
//	 * @param verbose - the verbose message to send to the logcat
//	 */
//	public static void v(String tag, String verbose) {
//		log(LogLevel.Verbose, tag, verbose);
//	}
//
//	/**
//	 * @param tag     - the tag name usually the class that we came from
//	 * @param warning - the warning message to send to the logcat
//	 */
//	public static void w(String tag, String warning) {
//		log(LogLevel.Warning, tag, warning);
//	}
//
//	/**
//	 * @param tag     - the tag name usually the class that we came from
//	 * @param warning - the warning message to send to the logcat
//	 */
//	public static void w(String tag, String warning, Throwable e) {
//		log(LogLevel.Warning, tag, warning, e);
//	}
//
//	/**
//	 * @param tag     - the tag name usually the class that we came from
//	 * @param warning - the warning message to send to the logcat
//	 */
//	public static void w(String tag, Throwable e) {
//		log(LogLevel.Warning, tag, e);
//	}
//
//	private Log() {}
//
//	public static void log(LogLevel logLevel, String tag, String message, Throwable e) {
//		if (e == null) {
//			log(logLevel, tag, message);
//			return;
//		}
//
//		if (!Log.DebugMode)
//			return;
//		tag = getPreTag(tag);
//		switch (logLevel) {
//			case Assert:
//			case Error:
//				android.util.Log.e(tag, message, e);
//				break;
//			case Warning:
//				android.util.Log.w(tag, message, e);
//				break;
//			case Info:
//				android.util.Log.i(tag, message, e);
//				break;
//			case Debug:
//				android.util.Log.d(tag, message, e);
//				break;
//			case Verbose:
//				android.util.Log.v(tag, message, e);
//				break;
//		}
//	}
//
//	public static void log(LogLevel logLevel, String tag, String message) {
//		if (!Log.DebugMode)
//			return;
//
//		tag = getPreTag(tag);
//		switch (logLevel) {
//			case Assert:
//			case Error:
//				android.util.Log.e(tag, message);
//				break;
//			case Warning:
//				android.util.Log.w(tag, message);
//				break;
//			case Info:
//				android.util.Log.i(tag, message);
//				break;
//			case Debug:
//				android.util.Log.d(tag, message);
//				break;
//			case Verbose:
//				android.util.Log.v(tag, message);
//				break;
//		}
//	}
//
//	public static void log(LogLevel logLevel, String tag, Throwable e) {
//		if (e == null)
//			return;
//
//		log(logLevel, tag, "", e);
//	}
//}

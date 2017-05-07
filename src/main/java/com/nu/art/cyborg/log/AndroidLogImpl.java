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
// * A convenience object to inherit from (in case your object does not have a parent) to have log api.
// */
//public class AndroidLogImpl
//		implements Logger {
//
//	/**
//	 * merged Social App & pdf sdk 10/07/2016
//	 * States whether this logger is enabled. In some cases you would like to enable or disable a component log, this will be the boolean to use for that!
//	 */
//	private boolean enabled = true;
//
//	private String tag = getClass().getSimpleName();
//
//	/**
//	 * @param enabled enable or disable this log instance.
//	 */
//	public final void setEnabled(boolean enabled) {
//		this.enabled = enabled;
//	}
//
//	public void setTAG(String tag) {
//		this.tag = tag;
//	}
//
//	@Override
//	public final void logVerbose(String verbose) {
//		if (enabled)
//			Log.v(tag, verbose);
//	}
//
//	@Override
//	public final void logDebug(String debug) {
//		if (enabled)
//			Log.d(tag, debug);
//	}
//
//	@Override
//	public final void logInfo(String info) {
//		if (enabled)
//			Log.i(tag, info);
//	}
//
//	@Override
//	public final void logWarning(String warning) {
//		if (enabled)
//			Log.w(tag, warning);
//	}
//
//	@Override
//	public final void logError(String error) {
//		if (enabled)
//			Log.e(tag, error);
//	}
//
//	@Override
//	public final void logError(String error, Throwable e) {
//		if (enabled)
//			Log.e(tag, error, e);
//	}
//
//	@Override
//	public final void logWarning(String warning, Throwable e) {
//		if (enabled)
//
//			Log.w(tag, warning, e);
//	}
//
//	@Override
//	public final void logError(Throwable e) {
//		if (enabled)
//			Log.e(tag, e);
//	}
//
//	@Override
//	public final void logVerbose(String verbose, Object... params) {
//		if (enabled)
//			Log.v(tag, String.format(verbose, params));
//	}
//
//	@Override
//	public final void logDebug(String debug, Object... params) {
//		if (enabled)
//			Log.d(tag, String.format(debug, params));
//	}
//
//	@Override
//	public final void logInfo(String info, Object... params) {
//		if (enabled)
//			Log.i(tag, String.format(info, params));
//	}
//
//	@Override
//	public final void logWarning(String warning, Object... params) {
//		if (enabled)
//			Log.w(tag, String.format(warning, params));
//	}
//
//	@Override
//	public final void logError(String error, Object... params) {
//		if (enabled)
//			Log.e(tag, String.format(error, params));
//	}
//}

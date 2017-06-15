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

package com.nu.art.cyborg.core;

import android.app.Application;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Handler;
import android.view.animation.Animation;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.common.interfaces.CyborgComponentDelegator;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.modular.core.ModuleItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Created by TacB0sS on 07-Jun 2016.
 */
public abstract class CyborgModuleItem
		extends ModuleItem
		implements CyborgComponentDelegator, ILogger {

	private final String TAG = getClass().getSimpleName();

	protected Cyborg cyborg;

	private ILogger logger;

	final void setCyborg(Cyborg cyborg) {
		this.cyborg = cyborg;
		logger = cyborg.getLogger(this);
	}

	@Override
	public final long elapsedTimeMillis() {
		return cyborg.elapsedTimeMillis();
	}

	@Override
	public final InputStream getRawResources(int resourceId) {
		return getResources().openRawResource(resourceId);
	}

	@Override
	public Application getApplication() {
		return cyborg.getApplication();
	}

	@Override
	public final Resources getResources() {
		return cyborg.getResources();
	}

	@Override
	public final ContentResolver getContentResolver() {
		return cyborg.getContentResolver();
	}

	@Override
	public final <Service> Service getSystemService(ServiceType<Service> service) {
		return cyborg.getSystemService(service);
	}

	@Override
	public final Locale getLocale() {
		return cyborg.getLocale();
	}

	@Override
	public final float getDimension(int dimensionId) {
		return cyborg.getDimension(dimensionId);
	}

	@Override
	public final float dimToPx(int type, float size) {
		return cyborg.dimToPx(type, size);
	}

	@Override
	public final int getColor(int colorId) {
		return cyborg.getColor(colorId);
	}

	@Override
	public final void postOnUI(long delay, Runnable action) {
		cyborg.postOnUI(delay, action);
	}

	@Override
	public final void removeAndPostOnUI(Runnable action) {
		cyborg.removeAndPostOnUI(action);
	}

	@Override
	public final void removeAndPostOnUI(long delay, Runnable action) {
		cyborg.removeAndPostOnUI(delay, action);
	}

	@Override
	public final void removeActionFromUI(Runnable action) {
		cyborg.removeActionFromUI(action);
	}

	@Override
	public final void postOnUI(Runnable action) {
		cyborg.postOnUI(action);
	}

	@Override
	public Animation loadAnimation(int animationId) {
		return cyborg.loadAnimation(animationId);
	}

	@Override
	public final Handler getUI_Handler() {
		return cyborg.getUI_Handler();
	}

	@Override
	public final void toastDebug(String text) {
		cyborg.toastDebug(text);
	}

	@Override
	public final void toastShort(int stringId, Object... args) {
		cyborg.toastShort(stringId, args);
	}

	@Override
	public final void toastLong(int stringId, Object... args) {
		cyborg.toastLong(stringId, args);
	}

	@Override
	public final void toastShort(StringResourceResolver stringResolver) {
		cyborg.toastShort(stringResolver);
	}

	@Override
	public final void toastLong(StringResourceResolver stringResolver) {
		cyborg.toastLong(stringResolver);
	}

	@Override
	public void sendEvent(String category, String action, String label, long value) {
		cyborg.sendEvent(category, action, label, value);
	}

	@Override
	public void sendException(String description, Throwable t, boolean crash) {
		cyborg.sendException(description, t, crash);
	}

	@Override
	public void sendView(String viewName) {
		cyborg.sendView(viewName);
	}

	@Override
	public final void vibrate(int repeat, long... interval) {
		cyborg.vibrate(repeat, interval);
	}

	@Override
	public final void vibrate(long ms) {
		cyborg.vibrate(ms);
	}

	@Override
	public final String convertNumericString(String numericString) {
		return cyborg.convertNumericString(numericString);
	}

	@Override
	public final InputStream getAsset(String assetName)
			throws IOException {
		return cyborg.getAsset(assetName);
	}

	@Override
	public final int dpToPx(int dp) {
		return cyborg.dpToPx(dp);
	}

	@Override
	public final String getString(int stringId, Object... params) {
		return cyborg.getString(stringId, params);
	}

	@Override
	public final String getString(StringResourceResolver stringResolver) {
		return cyborg.getString(stringResolver);
	}

	@Override
	public final String getPackageName() {
		return cyborg.getPackageName();
	}

	@Override
	public final boolean isDebug() {
		return cyborg.isDebug();
	}

	@Override
	public final boolean isDebugCertificate() {
		return cyborg.isDebugCertificate();
	}

	@Override
	public final void waitForDebugger() {
		cyborg.waitForDebugger();
	}

	@Override
	public final void postActivityAction(ActivityStackAction action) {
		cyborg.postActivityAction(action);
	}

	@Override
	public final <ListenerType> void dispatchEvent(String message, final Class<ListenerType> listenerType, final Processor<ListenerType> processor) {
		logDebug("Dispatching UI Event: " + message);
		cyborg.dispatchEvent(message, listenerType, processor);
	}

	@Override
	public void logVerbose(String verbose) {
		if (logger != null)
			logger.logVerbose(verbose);
	}

	@Override
	public void logVerbose(String verbose, Object... params) {
		if (logger != null)
			logger.logVerbose(verbose, params);
	}

	@Override
	public void logVerbose(Throwable e) {
		if (logger != null)
			logger.logVerbose(e);
	}

	@Override
	public void logVerbose(String verbose, Throwable e) {
		if (logger != null)
			logger.logVerbose(verbose, e);
	}

	@Override
	public void logDebug(String debug) {
		if (logger != null)
			logger.logDebug(debug);
	}

	@Override
	public void logDebug(String debug, Object... params) {
		if (logger != null)
			logger.logDebug(debug, params);
	}

	@Override
	public void logDebug(Throwable e) {
		if (logger != null)
			logger.logDebug(e);
	}

	@Override
	public void logDebug(String debug, Throwable e) {
		if (logger != null)
			logger.logDebug(debug, e);
	}

	@Override
	public void logInfo(String info) {
		if (logger != null)
			logger.logInfo(info);
	}

	@Override
	public void logInfo(String info, Object... params) {
		if (logger != null)
			logger.logInfo(info, params);
	}

	@Override
	public void logInfo(Throwable e) {
		if (logger != null)
			logger.logInfo(e);
	}

	@Override
	public void logInfo(String info, Throwable e) {
		if (logger != null)
			logger.logInfo(info, e);
	}

	@Override
	public void logWarning(String warning) {
		if (logger != null)
			logger.logWarning(warning);
	}

	@Override
	public void logWarning(String warning, Object... params) {
		if (logger != null)
			logger.logWarning(warning, params);
	}

	@Override
	public void logWarning(Throwable e) {
		if (logger != null)
			logger.logWarning(e);
	}

	@Override
	public void logWarning(String warning, Throwable e) {
		if (logger != null)
			logger.logWarning(warning, e);
	}

	@Override
	public void logError(String error) {
		if (logger != null)
			logger.logError(error);
	}

	@Override
	public void logError(String error, Object... params) {
		if (logger != null)
			logger.logError(error, params);
	}

	@Override
	public void logError(Throwable e) {
		if (logger != null)
			logger.logError(e);
	}

	@Override
	public void logError(String error, Throwable e) {
		if (logger != null)
			logger.logError(error, e);
	}
}

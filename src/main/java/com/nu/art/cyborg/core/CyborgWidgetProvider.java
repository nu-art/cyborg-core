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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.view.animation.Animation;

import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.common.interfaces.ICyborgModule;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.modular.core.Module;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Just like Android's WidgetProvider, but allows you to predefine a desired module to work with, assuming that most of the time a WidgetProvider would be
 * using
 * one module!
 *
 * @param <_ModuleType> Type of the {@link CyborgModule} to use in this WidgetProvider.
 */
@SuppressWarnings( {
	                   "unused",
	                   "NullableProblems"
                   })
public abstract class CyborgWidgetProvider<_ModuleType extends CyborgModule>
	extends AppWidgetProvider
	implements ICyborgModule, ILogger {

	private String tag = getClass().getSimpleName();

	private final Class<_ModuleType> moduleType;

	private Cyborg cyborg;

	private Logger logger;

	protected CyborgWidgetProvider(Class<_ModuleType> moduleType) {
		this.moduleType = moduleType;
	}

	private void init(Context context) {
		this.cyborg = CyborgBuilder.getInstance();
		logger = cyborg.getLogger(this);
	}

	@Override
	public final long elapsedTimeMillis() {
		return cyborg.elapsedTimeMillis();
	}

	@Override
	public final void onReceive(Context context, Intent intent) {
		init(context);
		onReceive(getModule(moduleType), context, intent);
	}

	@Override
	public final void onDeleted(Context context, int[] appWidgetIds) {
		init(context);
		onDeleted(getModule(moduleType), context, appWidgetIds);
	}

	@Override
	public final void onDisabled(Context context) {
		init(context);
		onDisabled(getModule(moduleType), context);
	}

	@Override
	public final void onEnabled(Context context) {
		init(context);
		onEnabled(getModule(moduleType), context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetModule, int[] appWidgetIds) {
		init(context);
		for (int appWidgetId : appWidgetIds) {
			logDebug("Updating widget id: " + appWidgetId);
			onUpdate(getModule(moduleType), context, appWidgetModule, appWidgetId);
		}
	}

	@Override
	public final IBinder peekService(Context myContext, Intent service) {
		init(myContext);
		return peekService(getModule(moduleType), myContext, service);
	}

	protected void onUpdate(_ModuleType moduleType, Context context, AppWidgetManager appWidgetModule, int appWidgetId) {}

	protected IBinder peekService(_ModuleType moduleType, Context myContext, Intent service) {
		return null;
	}

	protected void onReceive(_ModuleType moduleType, Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	protected void onDeleted(_ModuleType moduleType, Context context, int[] appWidgetIds) {}

	protected void onDisabled(_ModuleType moduleType, Context context) {}

	protected void onEnabled(_ModuleType moduleType, Context context) {}

	@Override
	public final InputStream getRawResources(int resourceId) {
		return getResources().openRawResource(resourceId);
	}

	@Override
	public Context getApplicationContext() {
		return cyborg.getApplicationContext();
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

	public final <ListenerType> void dispatchEvent(String message, final Processor<ListenerType> processor) {
		cyborg.dispatchEvent(logger, message, processor);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return cyborg.getModule(moduleType);
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

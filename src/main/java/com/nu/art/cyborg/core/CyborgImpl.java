/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
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

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.Logger;
import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.exceptions.runtime.MUST_NeverHappenException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;
import com.nu.art.cyborg.common.utils.GenericServiceConnection;
import com.nu.art.cyborg.common.utils.GenericServiceConnection.ServiceConnectionListenerImpl;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgBuilder.LaunchConfiguration;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.modules.AndroidLogClient;
import com.nu.art.cyborg.core.modules.IAnalyticsModule;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.cyborg.modules.AppDetailsModule;
import com.nu.art.cyborg.modules.VibrationModule;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModuleManager;
import com.nu.art.modular.core.ModuleManager.ModuleInjector;
import com.nu.art.modular.core.ModulesPack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * This is an internal object.
 */
final class CyborgImpl
	extends Logger
	implements Cyborg {

	private final HashMap<Class<? extends Service>, GenericServiceConnection<? extends Service>> serviceConnections = new HashMap<>();

	private final long CurrentElapsedDelta = SystemClock.elapsedRealtime() - System.currentTimeMillis();

	private final ArrayList<Processor<Cyborg>> completionListeners;

	private final WeakReference<Context> applicationRef;

	private final Handler uiHandler;

	private List<String> permissionsInManifest;

	private ActivityStack activityStackHandler;

	private ModuleManager moduleManager;

	private AppMeta meta;

	private ReceiversManager receiversManager;

	private boolean loaded;

	private LaunchConfiguration launchConfiguration;

	static boolean inEditMode;
	private long startupDuration;

	public CyborgImpl(Context application, LaunchConfiguration launchConfiguration) {
		this.applicationRef = new WeakReference<>(application);
		this.launchConfiguration = launchConfiguration;
		this.completionListeners = new ArrayList<>();
		this.uiHandler = new Handler();
	}

	@Override
	public boolean isPermissionDeclared(String permission) {
		return permissionsInManifest.contains(permission);
	}

	@Override
	public final boolean isInEditMode() {
		return inEditMode;
	}

	@Override
	public LaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	public final void addCompletionProcessor(Processor<Cyborg> processor) {
		synchronized (CyborgBuilder.class) {
			completionListeners.add(processor);
		}
	}

	@SuppressWarnings("unchecked")
	final void init(Class<? extends ModulesPack>... modulesPacks) {
		long startedAt = System.currentTimeMillis();

		if (loaded)
			throw new BadImplementationException("Trying to load Cyborg for the second time!");

		meta = new AppMeta();
		meta.populate();

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
			permissionsInManifest = ArrayTools.asList(packageInfo.requestedPermissions);
		} catch (NameNotFoundException e) {
			throw new MUST_NeverHappenException("", e);
		}

		activityStackHandler = new ActivityStack(CyborgImpl.this);
		receiversManager = new ReceiversManager(CyborgImpl.this);

		BeLogged.getInstance().addClient(new AndroidLogClient());
		logVerbose(" Application Created...");
		moduleManager = new ModuleManager();
		new CyborgModulesBuilder().setCyborg(this).addModulePacks(modulesPacks).build(moduleManager);

		if (!inEditMode) {
			dispatchOnLoadingCompleted();
		}

		startupDuration = System.currentTimeMillis() - startedAt;
	}

	@Override
	public ModuleInjector getModuleInjector() {
		return moduleManager.getInjector();
	}

	@SuppressWarnings("unchecked")
	private void dispatchOnLoadingCompleted() {
		synchronized (CyborgBuilder.class) {
			loaded = true;
			Processor<Cyborg>[] listeners = ArrayTools.asArray(completionListeners, Processor.class);
			for (Processor<Cyborg> processor : listeners) {
				processor.process(this);
			}
			completionListeners.clear();
		}
	}

	@Override
	public final void setActivityInForeground(CyborgActivityBridge activityBridge) {
		activityStackHandler.setActivityBridge(activityBridge);
	}

	@Override
	public final void openActivityInStackForResult(final Intent intent, final int requestCode) {
		postActivityAction(new ActivityStackAction() {

			@Override
			public void execute(CyborgActivityBridge activity) {
				activity.startActivityForResult(intent, requestCode);
			}
		});
	}

	@Override
	public final void openActivityInStack(final Intent intent) {
		postActivityAction(new ActivityStackAction() {

			@Override
			public void execute(CyborgActivityBridge activity) {
				activity.startActivity(intent);
			}
		});
	}

	@Override
	public final void postActivityAction(ActivityStackAction action) {
		activityStackHandler.addItem(action);
	}

	public long getStartupDuration() {
		return startupDuration;
	}

	public final boolean isSuperUser() {
		File pathToSuperUserFile = new File("/system/xbin/su");
		return pathToSuperUserFile.exists();
	}

	/*
	 * Resources
	 */
	@Override
	public final Resources getResources() {
		return getApplicationContext().getResources();
	}

	@Override
	public Configuration getConfiguration() {
		return getApplicationContext().getResources().getConfiguration();
	}

	@Override
	public final InputStream getRawResources(int resourceId) {
		return getResources().openRawResource(resourceId);
	}

	@Override
	public final InputStream getAsset(String assetName)
		throws IOException {
		return getApplicationContext().getAssets().open(assetName);
	}

	@Override
	public int dpToPx(int dp) {
		return dpTo(TypedValue.COMPLEX_UNIT_DIP, dp);
	}

	public int dpTo(int originUnit, int value) {
		return (int) TypedValue.applyDimension(originUnit, value, getResources().getDisplayMetrics());
	}

	@Override
	public final String getString(StringResourceResolver stringResolver) {
		return stringResolver.getString(this);
	}

	@Override
	public final String convertNumericString(String numericString) {
		for (int i = 0; i < numericIds.length; i++) {
			String number = getString(numericIds[i]);
			numericString = numericString.replace("" + i, number);
		}
		return numericString;
	}

	@Override
	public final String getString(int stringId, Object... args) {
		return getApplicationContext().getString(stringId, args);
	}

	@Override
	public float getDimension(int dimensionId) {
		return getResources().getDimension(dimensionId);
	}

	@Override
	public Locale getLocale() {
		return getResources().getConfiguration().locale;
	}

	@Override
	public int getColor(int colorId) {
		return getResources().getColor(colorId);
	}

	@Override
	public float dimToPx(int type, float size) {
		return TypedValue.applyDimension(type, size, getResources().getDisplayMetrics());
	}

	/*
	 * Analytics
	 */
	@Override
	public final void sendView(final String viewName) {
		String message = "Analytics Screen: " + viewName;
		dispatchModuleEvent(this, message, IAnalyticsModule.class, new Processor<IAnalyticsModule>() {

			@Override
			public void process(IAnalyticsModule module) {
				module.sendView(viewName);
			}
		});
	}

	@Override
	public final void sendEvent(final String category, final String action, final String label, final long value) {
		String message = "Analytics Event: " + category + " - " + action + " - " + label + " - " + value;
		dispatchModuleEvent(this, message, IAnalyticsModule.class, new Processor<IAnalyticsModule>() {

			@Override
			public void process(IAnalyticsModule module) {
				module.sendEvent(category, action, label, value);
			}
		});
	}

	@Override
	public final void sendException(final String description, final Throwable t, final boolean crash) {
		String message = "Analytics Exception: " + description;
		dispatchModuleEvent(this, message, IAnalyticsModule.class, new Processor<IAnalyticsModule>() {

			@Override
			public void process(IAnalyticsModule module) {
				module.sendException(description, t, crash);
			}
		});
	}

	/*
	 * Modules
	 */
	@Override
	public final <Type> Type[] getModulesAssignableFrom(Class<Type> moduleType) {
		return moduleManager.getModulesAssignableFrom(moduleType);
	}

	@Override
	public Logger getLogger(Object beLogged) {
		return BeLogged.getInstance().getLogger(beLogged);
	}

	@Override
	public final <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return moduleManager.getModule(moduleType);
	}

	/*
	 * App MetaData
	 */
	@Override
	public String getValueFromManifest(String key, String defaultValue) {
		return meta.metaData.getString(key, defaultValue);
	}

	@Override
	public final String getPackageName() {
		return meta.packageName;
	}

	@Override
	public final String getName() {
		return meta.name;
	}

	@Override
	public final String getVersionName() {
		return meta.version;
	}

	@Override
	public final int getVersionCode() {
		return meta.versionCode;
	}

	/*
	 * App System Components
	 */
	@Override
	public final PackageManager getPackageManager() {
		return getApplicationContext().getPackageManager();
	}

	@Override
	public final ContentResolver getContentResolver() {
		return getApplicationContext().getContentResolver();
	}

	@Override
	public final Context getApplicationContext() {
		return applicationRef.get();
	}

	@Override
	public final void startActivity(Intent intent) {
		getApplicationContext().startActivity(intent);
	}

	@Override
	@SuppressWarnings( {
		                   "unchecked",
		                   "ResourceType"
	                   })
	public final <SystemService> SystemService getSystemService(ServiceType<SystemService> service) {
		Context applicationContext = getApplicationContext();
		if (isInEditMode())
			return null;

		return (SystemService) applicationContext.getSystemService(service.getKey());
	}

	@Override
	public final void registerReceiver(Class<? extends CyborgReceiver<?>> receiverType, String... actions) {
		receiversManager.registerReceiver(receiverType, actions);
	}

	@Override
	public final void unregisterReceiver(Class<? extends CyborgReceiver<?>> receiverType) {
		receiversManager.unregisterReceiver(receiverType);
	}

	@Override
	public void bindService(Intent serviceIntent, ServiceConnection serviceConnection, int flags) {
		getApplicationContext().bindService(serviceIntent, serviceConnection, flags);
	}

	@Override
	public void unbindService(ServiceConnection serviceConnection) {
		getApplicationContext().unbindService(serviceConnection);
	}

	@Override
	public final ComponentName startService(Intent serviceIntent) {
		return getApplicationContext().startService(serviceIntent);
	}

	@Override
	public void startService(Class<? extends Service> serviceType) {
		ComponentName componentName = getApplicationContext().startService(new Intent(getApplicationContext(), serviceType));
		if (componentName == null)
			throw ExceptionGenerator.developerDidNotAddTheServiceToTheManifest(serviceType);
	}

	@Override
	public void stopService(Class<? extends Service> serviceType) {
		getApplicationContext().stopService(new Intent(getApplicationContext(), serviceType));
	}

	@SuppressWarnings("unchecked")
	private <_ServiceType extends Service> GenericServiceConnection<_ServiceType> getServiceConnection(Class<_ServiceType> cls) {

		GenericServiceConnection<_ServiceType> connection = (GenericServiceConnection<_ServiceType>) serviceConnections.get(cls);

		if (connection == null) {
			serviceConnections.put(cls, connection = new GenericServiceConnection<>(cls));
			Intent serviceIntent = new Intent(getApplicationContext(), cls);
			getApplicationContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
		}

		return connection;
	}

	@Override
	public <_ServiceType extends Service> void startForegroundService(final Class<_ServiceType> cls, final int id, final Notification notification) {
		getServiceConnection(cls).addListener(new ServiceConnectionListenerImpl<_ServiceType>() {
			@Override
			public void onServiceConnected(_ServiceType service) {
				startService(cls);
				service.startForeground(id, notification);
			}

			@Override
			public void onServiceDisconnected(_ServiceType service) {
				service.stopForeground(true);
			}
		});
	}

	@Override
	public <_ServiceType extends Service> void stopForegroundService(final Class<_ServiceType> cls, boolean dismiss) {
		getServiceConnection(cls).addListener(new ServiceConnectionListenerImpl<_ServiceType>() {
			@Override
			public void onServiceConnected(_ServiceType service) {
				service.stopForeground(true);
				stopService(cls);
			}

			@Override
			public void onServiceDisconnected(_ServiceType service) {
				service.stopForeground(true);
			}
		});
	}

	/*
	 * Vibrator
	 */
	@Override
	public final void vibrate(long ms) {
		getModule(VibrationModule.class).vibrateImpl(ms);
	}

	@Override
	public final void vibrate(int repeat, long... interval) {
		getModule(VibrationModule.class).vibrateImpl(repeat, interval);
	}

	/*
	 * UI
	 */
	@Override
	public final void postOnUI(long delay, Runnable action) {
		getUI_Handler().postDelayed(action, delay);
	}

	@Override
	public final void removeAndPostOnUI(Runnable action) {
		removeActionFromUI(action);
		postOnUI(action);
	}

	@Override
	public final void removeAndPostOnUI(long delay, Runnable action) {
		removeActionFromUI(action);
		postOnUI(delay, action);
	}

	@Override
	public final void removeActionFromUI(Runnable action) {getUI_Handler().removeCallbacks(action);}

	@Override
	public final void postOnUI(Runnable action) {
		getUI_Handler().post(action);
	}

	@Override
	public void assertMainThread() {
		if (!isMainThread())
			throw new BadImplementationException("---- MUST BE CALLED ON A UI THREAD ----  Method was called on the '" + Thread.currentThread().getName() + "'");
	}

	@Override
	public final Handler getUI_Handler() {
		return uiHandler;
	}

	@Override
	public final boolean isMainThread() {
		return Thread.currentThread() == Looper.getMainLooper().getThread();
	}

	/*
	 * Toast
	 */
	private void showToast(int length, String toToast) {
		final Toast toast = Toast.makeText(getApplicationContext(), toToast, length);
		toast.show();
	}

	@Override
	public final void toastDebug(String toastMessage) {
		if (!isDebug())
			return;
		_toast(Toast.LENGTH_LONG, "DEBUG: " + toastMessage);
	}

	@Override
	public final void toastShort(int stringId, Object... args) {
		_toast(Toast.LENGTH_SHORT, stringId, args);
	}

	@Override
	public final void toastLong(int stringId, Object... args) {
		_toast(Toast.LENGTH_LONG, stringId, args);
	}

	@Override
	public final void toastShort(StringResourceResolver stringResolver) {
		_toast(Toast.LENGTH_SHORT, getString(stringResolver));
	}

	@Override
	public final void toastLong(StringResourceResolver stringResolver) {
		_toast(Toast.LENGTH_LONG, getString(stringResolver));
	}

	private void _toast(final int length, int stringId, Object... args) {
		final String text = getString(stringId, args);
		_toast(length, text);
	}

	private void _toast(final int length, final String text) {
		if (Thread.currentThread() == getApplicationContext().getMainLooper().getThread()) {
			showToast(length, text);
		} else
			getUI_Handler().post(new Runnable() {

				@Override
				public void run() {
					showToast(length, text);
				}
			});
	}

	/*
	 * Debug
	 */
	@Override
	public final boolean isDebugCertificate() {
		return getModule(AppDetailsModule.class).getCertificate().isDebugCertificate();
	}

	@Override
	public final boolean isDebug() {
		return inEditMode || getModule(AppDetailsModule.class).isDebuggable();
	}

	@Override
	public final void waitForDebugger() {
		if (isDebugCertificate())
			Debug.waitForDebugger();
	}

	@Override
	public final long elapsedTimeMillis() {
		return System.currentTimeMillis() + CurrentElapsedDelta;
	}

	@Override
	public <ListenerType> void dispatchEvent(ILogger originator, String message, Class<ListenerType> listenerType, Processor<ListenerType> processor) {
		activityStackHandler.dispatchEvent(originator, message, listenerType, processor);
	}

	@Override
	public final <ListenerType> void dispatchModuleEvent(ILogger originator,
	                                                     final String message,
	                                                     Class<ListenerType> listenerType,
	                                                     final Processor<ListenerType> processor) {
		moduleManager.dispatchModuleEvent(originator, message, listenerType, processor);
	}

	@Override
	public Animation loadAnimation(int animationId) {
		return AnimationUtils.loadAnimation(getApplicationContext(), animationId);
	}

	<ListenerType> void dispatchGlobalEvent(Logger logger, String message, Class<ListenerType> listenerType, Processor<ListenerType> processor) {
		if (logger != null)
			logger.logInfo("Dispatching global event: " + message);
		dispatchModuleEvent(null, message, listenerType, processor);
		dispatchEvent(null, message, listenerType, processor);
	}

	private class AppMeta {

		private String name;

		private String version;

		private String packageName;

		private int versionCode;

		private Bundle metaData;

		public AppMeta() {
		}

		private void populate() {
			packageName = applicationRef.get().getPackageName();
			if (getPackageManager() != null) {
				PackageInfo packageInfo;
				ApplicationInfo info;
				try {
					packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
					info = getPackageManager().getApplicationInfo(getPackageName(), 0);
				} catch (NameNotFoundException e) {
					throw new MUST_NeverHappenException("Could not find my own package");
				}

				if (info == null)
					// we are in edit mode
					name = "NoName";
				else {
					name = info.name;
					this.metaData = packageInfo.applicationInfo.metaData;
				}

				if (packageInfo == null) {
					// we are in edit mode
					version = "no version";
					versionCode = 0;
				} else {
					version = packageInfo.versionName;
					versionCode = packageInfo.versionCode;
				}
			}
		}
	}
}

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

package com.nu.art.cyborg.core.abs;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.IBinder;

import com.nu.art.belog.Logger;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgActivityBridge;
import com.nu.art.cyborg.core.CyborgBuilder.LaunchConfiguration;
import com.nu.art.cyborg.core.CyborgModuleManager.CyborgModuleInjector;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.modular.interfaces.ModuleManagerDelegator;

public interface Cyborg
		extends CyborgDelegator, _LifeCycleLogger, _AppMeta, ModuleManagerDelegator {

	Application getApplication();

	PackageManager getPackageManager();

	void registerReceiver(Class<? extends CyborgReceiver<?>> receiverType, String[] actions);

	void unregisterReceiver(Class<? extends CyborgReceiver<?>> receiverType);

	void openActivityInStackForResult(Intent intent, int requestCode);

	void openActivityInStack(Intent intent);

	void startActivity(Intent intent);

	boolean isMainThread();

	ComponentName startService(Intent serviceIntent);

	void setActivityInForeground(CyborgActivityBridge activityBridge);

	void postActivityAction(ActivityStackAction activityStackAction);

	CyborgModuleInjector getModuleInjector();

	LaunchConfiguration getLaunchConfiguration();

	boolean isInEditMode();

	void assertMainThread();

	Configuration getConfiguration();

	void bindService(Intent serviceIntent, ServiceConnection serviceConnection, int flags);

	void unbindService(ServiceConnection serviceConnection);

	<Type> Type[] getModulesAssignableFrom(Class<Type> parentType);

	ILogger getLogger(Object beLogged);

	void setBeLogged(Logger logger);
}

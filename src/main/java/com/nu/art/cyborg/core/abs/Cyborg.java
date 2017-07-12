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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.core.CyborgActivityBridge;
import com.nu.art.cyborg.core.CyborgBuilder.LaunchConfiguration;
import com.nu.art.cyborg.core.CyborgReceiver;
import com.nu.art.modular.core.ModuleManager.ModuleInjector;
import com.nu.art.modular.interfaces.ModuleManagerDelegator;

public interface Cyborg
		extends CyborgDelegator, _LifeCycleLogger, _AppMeta, ModuleManagerDelegator {

	/**
	 * @return The application instance of the app
	 */
	Context getApplicationContext();

	/**
	 * @return Android's {@link PackageManager}
	 */
	PackageManager getPackageManager();

	/**
	 * Register a receiver if not already present in the manifest.
	 *
	 * @param receiverType The class type of the receiver.
	 * @param actions      The list of actions the receiver will respond to.
	 */
	void registerReceiver(Class<? extends CyborgReceiver<?>> receiverType, String... actions);

	/**
	 * Unregister the receiver of the specified class type.
	 *
	 * @param receiverType The class type of the receiver.
	 */
	void unregisterReceiver(Class<? extends CyborgReceiver<?>> receiverType);

	/**
	 * In case you use a single instance activity, this workaround would delegate and return the result to your original activity.
	 *
	 * @param intent      The intent to start the activity for.
	 * @param requestCode the request code for the result.
	 */
	void openActivityInStackForResult(Intent intent, int requestCode);

	/**
	 * Start an Android activity, upon the next activity in the stack.
	 *
	 * @param intent Intent to start the activity.
	 */
	void openActivityInStack(Intent intent);

	/**
	 * Start an Android Activity according to the provided intent
	 *
	 * @param intent Intent to start the activity.
	 */
	void startActivity(Intent intent);

	/**
	 * @param activityBridge sets the activity in the foreground
	 */
	void setActivityInForeground(CyborgActivityBridge activityBridge);

	/**
	 * @return The instance of the module injector.
	 */
	ModuleInjector getModuleInjector();

	/**
	 * @return The launch configuration of Cyborg
	 */
	LaunchConfiguration getLaunchConfiguration();

	/**
	 * @return whether this is an edit mode instance
	 */
	boolean isInEditMode();

	/**
	 * @return Whether or not the executing is the main thread.
	 */
	boolean isMainThread();

	/**
	 * Crash the app if this is not the main thread
	 */
	void assertMainThread();

	/**
	 * @return Android current configuration
	 */
	Configuration getConfiguration();

	/**
	 * Start a service according to the respective intent
	 *
	 * @param serviceIntent The intent to start the service with
	 *
	 * @return A component name of the service.
	 */
	ComponentName startService(Intent serviceIntent);

	/**
	 * Bind to a service.
	 *
	 * @param serviceIntent     The intent to start the service with
	 * @param serviceConnection The connection to manage the service connectivity state.
	 * @param flags             Other flags for the service.
	 */
	void bindService(Intent serviceIntent, ServiceConnection serviceConnection, int flags);

	/**
	 * Unbind from a service.
	 *
	 * @param serviceConnection The connection of the service to disconnect from.
	 */
	void unbindService(ServiceConnection serviceConnection);

	/**
	 * @param parentType parent class to match with
	 * @param <Type>     The Type of Modules
	 *
	 * @return an array of the modules answering the parentType
	 */
	<Type> Type[] getModulesAssignableFrom(Class<Type> parentType);

	/**
	 * @param beLogged The object to be logged
	 *
	 * @return The ILogger for the object to belogged.
	 */
	ILogger getLogger(Object beLogged);

	/**
	 * Apparently when in the process of migrating an Android project that is stupidly coupled to Android classes) to Cyborg, you'd find this API really useful
	 * to contact your module and update it.
	 *
	 * If you are calling this keep in mind that you are doing something wrong, the logic which calls this needs to be encapsulated within a module... That's what
	 * they are for!
	 *
	 * @param message        A log message to accompany the event.
	 * @param listenerType   The listener type entities need to implement to receive the event.
	 * @param processor      A processor on how ti handle the event.
	 * @param <ListenerType> A generic bound to the listener type
	 */
	<ListenerType> void dispatchModuleEvent(final String message, final Class<ListenerType> listenerType, final Processor<ListenerType> processor);
}

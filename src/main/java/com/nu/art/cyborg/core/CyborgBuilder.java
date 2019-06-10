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

import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;

import com.nu.art.belog.BeConfig;
import com.nu.art.core.exceptions.runtime.DontCallThisException;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.modules.AndroidLogger;
import com.nu.art.cyborg.core.modules.CyborgBasePack;
import com.nu.art.cyborg.core.modules.CyborgEditModePack;
import com.nu.art.cyborg.errorMessages.ExceptionGenerator;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModulesPack;

import java.util.ArrayList;

public final class CyborgBuilder {

	public static final class LaunchConfiguration {

		final int layoutId;

		final String screenName;

		final Class<? extends CyborgActivity> activityType;

		public LaunchConfiguration(int layoutId, String screenName, Class<? extends CyborgActivity> activityType) {
			super();
			this.layoutId = layoutId;
			this.screenName = screenName;
			this.activityType = activityType;
		}
	}

	@SuppressWarnings("unchecked")
	public static final class CyborgConfiguration {

		private Context application;

		private BeConfig logConfig = AndroidLogger.Config_FastAndroidLogger;

		private LaunchConfiguration launchConfiguration;

		private Class<? extends ModulesPack>[] modulesPacks;

		public CyborgConfiguration(Context application) {
			this.application = application;
		}

		public CyborgConfiguration setModulesPacks(Class<? extends ModulesPack>... modulesPacksTypes) {
			ArrayList<Class<? extends ModulesPack>> modulesPacks = new ArrayList<>();
			boolean addBasePack = true;
			for (Class<? extends ModulesPack> modulesPacksType : modulesPacksTypes) {
				if (CyborgBasePack.class.isAssignableFrom(modulesPacksType))
					addBasePack = false;
				modulesPacks.add(modulesPacksType);
			}

			if (addBasePack)
				modulesPacks.add(0, CyborgBasePack.class);

			this.modulesPacks = (Class<? extends ModulesPack>[]) ArrayTools.asArray(modulesPacks, Class.class);
			return this;
		}

		public void setLogConfig(BeConfig logConfig) {
			this.logConfig = logConfig;
		}

		public CyborgConfiguration setLaunchConfiguration(int layoutId) {
			return setLaunchConfiguration(layoutId, "RootLayout");
		}

		public CyborgConfiguration setLaunchConfiguration(int layoutId, String screenName) {
			return setLaunchConfiguration(layoutId, screenName, CyborgActivity.class);
		}

		public CyborgConfiguration setLaunchConfiguration(int layoutId, String screenName, Class<? extends CyborgActivity> activityType) {
			return setLaunchConfiguration(new LaunchConfiguration(layoutId, screenName, activityType));
		}

		public CyborgConfiguration setLaunchConfiguration(LaunchConfiguration launchConfiguration) {
			this.launchConfiguration = launchConfiguration;
			return this;
		}
	}

	private static CyborgImpl instance;

	private CyborgBuilder() {
		throw new DontCallThisException("Stateless static");
	}

	public static void handleAttributes(Object object, Context context, AttributeSet attrs) {
		if (attrs == null)
			return;

		Cyborg cyborg;
		try {
			cyborg = CyborgBuilder.getInstance();
		} catch (Exception e) {
			return;
		}

		AttributeModule attributesManager = cyborg.getModule(AttributeModule.class);
		attributesManager.setAttributes(context, attrs, object);
	}

	/**
	 * This method is <b>ONLY</b> for refactoring purposes.<Br>
	 * In a perfect world, all the entities(Modules, Controllers, BroadcastReceivers, e.g.) in an app would use Cyborg's infra and will have access to the
	 * framework API to get modules, and plenty of other convenient API, but in the meanwhile, while you refactor your app to use Cyborg, you can call this api in
	 * order to break pieces of your code at a time, and not completely
	 *
	 * @param moduleType   The module type you want to get.
	 * @param context      You application context.
	 * @param <ModuleType> The generic module type
	 *
	 * @return The module instance mapped to the provided class
	 */
	@Deprecated
	public static <ModuleType extends Module> ModuleType getModule(Context context, Class<ModuleType> moduleType) {
		return getCyborg(context).getModule(moduleType);
	}

	/**
	 * This method is <b>ONLY</b> for refactoring purposes.<Br>
	 * In a perfect world, all the entities(Modules, Controllers, BroadcastReceivers, e.g.) in an app would use Cyborg's infra and will have access to the
	 * framework API to get modules, and plenty of other convenient API, but in the meanwhile, while you refactor your app to use Cyborg, you can call this api in
	 * order to break pieces of your code at a time, and not completely
	 *
	 * @param moduleType   The module type you want to get.
	 * @param <ModuleType> The generic module type
	 *
	 * @return The module instance mapped to the provided class
	 */
	@Deprecated
	public static <ModuleType extends Module> ModuleType getModule(Class<ModuleType> moduleType) {
		return getInstance().getModule(moduleType);
	}

	private static Cyborg getCyborg(Context context) {
		if (context != null) {
			return CyborgBuilder.getInEditMode(context);
		}
		return CyborgBuilder.getInstance();
	}

	public synchronized static Cyborg getInstance() {
		if (instance == null)
			throw ExceptionGenerator.cyborgWasNotInitializedProperly();
		return instance;
	}

	public synchronized static long getStartupDuration() {
		return instance.getStartupDuration();
	}

	@SuppressWarnings("unchecked")
	public synchronized static Cyborg getInEditMode(Context context) {
		if (instance != null)
			return instance;
		CyborgImpl.inEditMode = true;
		CyborgBuilder.startCyborg(new CyborgConfiguration(context).setModulesPacks(CyborgEditModePack.class));
		return CyborgBuilder.getInstance();
	}

	@SuppressWarnings("unchecked")
	public synchronized static void startCyborg(final CyborgConfiguration configuration) {
		if (Thread.currentThread() != Looper.getMainLooper().getThread())
			throw ExceptionGenerator.cyborgWasInitializedFromTheWrongThread();

		if (instance != null)
			throw ExceptionGenerator.cyborgWasInitializedForTheSecondTime();

		instance = new CyborgImpl(configuration.application, configuration.launchConfiguration);
		instance.init(configuration.modulesPacks);
	}
}

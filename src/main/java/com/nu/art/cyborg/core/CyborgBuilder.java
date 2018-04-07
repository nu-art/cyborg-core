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

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.modules.CyborgBasePack;
import com.nu.art.cyborg.core.modules.CyborgEditModePack;
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

		private LaunchConfiguration launchConfiguration;

		private Class<? extends ModulesPack>[] modulesPacks;

		public CyborgConfiguration(Context application, Class<? extends ModulesPack>... modulesPacks) {
			this(application, null, modulesPacks);
		}

		public CyborgConfiguration(Context application, int firstActivityLayoutId, Class<? extends ModulesPack>... modulesPacks) {
			this(application, new LaunchConfiguration(firstActivityLayoutId, "First Screen", CyborgActivity.class), modulesPacks);
		}

		public CyborgConfiguration(Context application, int firstActivityLayoutId, String screenName, Class<? extends ModulesPack>... modulesPacks) {
			this(application, new LaunchConfiguration(firstActivityLayoutId, screenName, CyborgActivity.class), modulesPacks);
		}

		public CyborgConfiguration(Context application, LaunchConfiguration launchConfig, Class<? extends ModulesPack>... modulesPacksTypes) {
			this.application = application;
			this.launchConfiguration = launchConfig;
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
		}
	}

	private static CyborgImpl instance;

	private CyborgBuilder() {
		throw new BadImplementationException("Stateless static");
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
			throw new BadImplementationException("MUST first called from the onCreate of your custom application class!");
		return instance;
	}

	@SuppressWarnings("unchecked")
	public synchronized static Cyborg getInEditMode(Context context) {
		if (instance != null)
			return instance;
		CyborgImpl.inEditMode = true;
		CyborgBuilder.startCyborg(new CyborgConfiguration(context, CyborgEditModePack.class));
		return CyborgBuilder.getInstance();
	}

	@SuppressWarnings("unchecked")
	public synchronized static void startCyborg(final CyborgConfiguration configuration) {
		if (Thread.currentThread() != Looper.getMainLooper().getThread())
			throw new BadImplementationException("Must be called from UI thread to be more specific from the onCreate of your custom application class!");

		if (instance != null)
			throw new BadImplementationException("Seriously?? You've already created Cyborg, what is the point of calling this method from two places?? call it only from your custom application onCreate method!!!");

		instance = new CyborgImpl(configuration.application, configuration.launchConfiguration);
		instance.init(configuration.modulesPacks);
	}

	public synchronized static void addCompletionProcessor(Processor<Cyborg> processor) {
		instance.addCompletionProcessor(processor);
	}
}

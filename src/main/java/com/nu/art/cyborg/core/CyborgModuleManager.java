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

import com.nu.art.belog.BeLogged;
import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModuleManager;
import com.nu.art.reflection.injector.Injector;
import com.nu.art.reflection.tools.ART_Tools;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * This is an internal object.
 */
public final class CyborgModuleManager
		extends ModuleManager {

	private ILogger logger;

	@SuppressWarnings("unchecked")
	final <Type> Type[] getModulesAssignableFrom(Class<Type> moduleType) {
		ArrayList<Type> toRet = new ArrayList<>();
		for (Module obj : getOrderedModules()) {
			if (!moduleType.isAssignableFrom(obj.getClass()))
				continue;

			toRet.add((Type) obj);
		}
		return ArrayTools.asArray(toRet, moduleType);
	}

	public final class CyborgModuleInjector
			extends Injector<CyborgModule, Object> {

		private CyborgModuleInjector() {}

		@Override
		@SuppressWarnings("unchecked")
		protected Object getValueForField(Field field) {
			if (field.getType() == CyborgModule.class)
				return null;

			CyborgModule module = getModule((Class<? extends CyborgModule>) field.getType());
			if (module == null)
				throw new ImplementationMissingException("You need to add the module of type: '" + field.getType() + "' to one of your ModulePacks");

			return module;
		}

		@Override
		protected Field[] extractFieldsFromInstance(Class<?> injecteeType) {
			return ART_Tools.getFieldsWithAnnotationAndTypeFromClassHierarchy(injecteeType, Object.class, null, null, CyborgModule.class);
		}
	}

	private final CyborgModuleInjector cyborgModuleInjector = new CyborgModuleInjector();

	protected final String TAG = getClass().getSimpleName();

	CyborgModuleManager() {
		super();
	}

	public final CyborgModuleInjector getInjector() {
		return cyborgModuleInjector;
	}

	@SuppressWarnings("unchecked")
	protected <ParentType> void dispatchModuleEvent(String message, Class<ParentType> parentType, Processor<ParentType> processor) {
		for (Module module : getOrderedModules()) {
			if (!parentType.isAssignableFrom(module.getClass()))
				continue;

			try {
				processor.process((ParentType) module);
			} catch (Throwable t) {
				String errorMessage = "Error while processing module event:\n   parentType: " + parentType.getSimpleName() + "\n   moduleType: " + module
						.getClass().getSimpleName();
				logger.logError(errorMessage, t);
			}
		}
	}

	@Override
	protected void onBuildCompleted() {
		logger = getModule(BeLogged.class).getLogger(this);
	}
}

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

import android.content.pm.FeatureInfo;

import com.nu.art.cyborg.common.consts.AnalyticsConstants;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.modules.AppDetailsModule;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModuleManagerBuilder;
import com.nu.art.modular.core.ModulesPack;

/**
 * This is an internal object.
 */
final class CyborgModulesBuilder
	extends ModuleManagerBuilder
	implements AnalyticsConstants {

	@SafeVarargs
	CyborgModulesBuilder(Class<? extends ModulesPack>... modulesPacksTypes) {
		super(CyborgModuleManager.class);
		setModulesPacksTypes(modulesPacksTypes);
	}

	private Cyborg cyborgImpl;

	protected AppDetailsModule configuration;

	final void setCyborg(CyborgImpl application) {
		this.cyborgImpl = application;
	}

	@SuppressWarnings("unused")
	private boolean checkFeature(String feature) {
		FeatureInfo[] features = cyborgImpl.getPackageManager().getSystemAvailableFeatures();
		for (FeatureInfo featureInfo : features) {
			if (featureInfo.name == null)
				continue;

			if (featureInfo.name.equals(feature))
				return true;
		}
		return false;
	}

	@Override
	protected void setupModule(Module module) {
		if (!(module instanceof CyborgModule))
			return;

		((CyborgModule) module).setCyborg(cyborgImpl);
	}

	final CyborgModuleManager getCyborgModuleManager() {
		return (CyborgModuleManager) moduleManager;
	}
}

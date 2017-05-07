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

package com.nu.art.cyborg.core.modules;

import com.nu.art.belog.BeLogged;
import com.nu.art.cyborg.core.CyborgRecyclerSetter;
import com.nu.art.cyborg.core.CyborgStackSetter;
import com.nu.art.cyborg.core.CyborgViewSetter;
import com.nu.art.cyborg.modules.AppDetailsModule;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.cyborg.modules.ImageUtilsModule;
import com.nu.art.cyborg.modules.VibrationModule;
import com.nu.art.cyborg.modules.custom.FontTypeSetter;
import com.nu.art.cyborg.ui.views.valueChanger.ValueChangerSetter;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModulesPack;

/**
 * Created by tacb0ss on 4/16/15.
 */
@SuppressWarnings("unchecked")
public class CyborgBasePack
		extends ModulesPack {

	private static final Class<? extends Module>[] modulesTypes = (Class<? extends Module>[]) new Class<?>[]{
			BeLogged.class,
			PreferencesModule.class,
			AppDetailsModule.class,
			DeviceDetailsModule.class,
			ImageUtilsModule.class,
			ThreadsModule.class,
			UtilsModule.class,
			VibrationModule.class,
			AttributeModule.class
	};

	protected CyborgBasePack() {
		super(modulesTypes);
	}

	@Override
	protected final void init() {
		getModule(BeLogged.class).addClient(new AndroidLogClient());
		getModule(AttributeModule.class).registerAttributesSetter(FontTypeSetter.class);
		getModule(AttributeModule.class).registerAttributesSetter(CyborgViewSetter.class);
		getModule(AttributeModule.class).registerAttributesSetter(CyborgStackSetter.class);
		getModule(AttributeModule.class).registerAttributesSetter(CyborgRecyclerSetter.class);
		getModule(AttributeModule.class).registerAttributesSetter(ValueChangerSetter.class);
	}
}

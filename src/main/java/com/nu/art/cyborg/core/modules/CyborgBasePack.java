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

package com.nu.art.cyborg.core.modules;

import com.nu.art.android.views.RoundedImageView.RoundedImageViewSetter;
import com.nu.art.cyborg.clipboard.ClipboardModule;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.CyborgRecycler.CyborgRecyclerSetter;
import com.nu.art.cyborg.core.CyborgStackController.CyborgStackSetter;
import com.nu.art.cyborg.core.CyborgView.CyborgViewSetter;
import com.nu.art.cyborg.core.CyborgViewPager.CyborgViewPagerSetter;
import com.nu.art.cyborg.modules.AppDetailsModule;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.cyborg.modules.CacheModule;
import com.nu.art.cyborg.modules.ImageUtilsModule;
import com.nu.art.cyborg.modules.PermissionModule;
import com.nu.art.cyborg.modules.VibrationModule;
import com.nu.art.cyborg.modules.custom.FontTypeSetter;
import com.nu.art.cyborg.ui.views.SquareView.SquareViewSetter;
import com.nu.art.cyborg.ui.views.valueChanger.ValueChangerSetter;
import com.nu.art.modular.core.Module;
import com.nu.art.modular.core.ModulesPack;
import com.nu.art.storage.PreferencesModule;

/**
 * Created by TacB0sS on 4/16/15.
 */
@SuppressWarnings("unchecked")
public class CyborgBasePack
	extends ModulesPack {

	private static final Class<? extends Module>[] modulesTypes = (Class<? extends Module>[]) new Class<?>[]{
		PreferencesModule.class,
		ClipboardModule.class,
		CacheModule.class,
		PermissionModule.class,
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
		getModule(PreferencesModule.class).setStorageFolder(CyborgBuilder.getInstance().getApplicationContext().getFilesDir().getAbsolutePath());
		AttributeModule attributeModule = getModule(AttributeModule.class);
		attributeModule.registerAttributesSetter(SquareViewSetter.class);
		attributeModule.registerAttributesSetter(FontTypeSetter.class);
		attributeModule.registerAttributesSetter(CyborgViewSetter.class);
		attributeModule.registerAttributesSetter(CyborgViewPagerSetter.class);
		attributeModule.registerAttributesSetter(CyborgStackSetter.class);
		attributeModule.registerAttributesSetter(CyborgRecyclerSetter.class);
		attributeModule.registerAttributesSetter(RoundedImageViewSetter.class);
		attributeModule.registerAttributesSetter(ValueChangerSetter.class);
	}
}

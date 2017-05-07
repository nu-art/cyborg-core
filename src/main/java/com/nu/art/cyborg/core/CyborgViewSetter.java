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

import android.content.res.TypedArray;

import com.nu.art.cyborg.R;
import com.nu.art.software.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.software.reflection.annotations.ReflectiveInitialization;
import com.nu.art.software.reflection.tools.ReflectiveTools;

/**
 * Setting the xml attributes onto a {@link CyborgView} instance. in this case creates the {@link CyborgController}'s  instance.
 */
@ReflectiveInitialization
public class CyborgViewSetter
		extends AttributesSetter<CyborgView> {

	private static int[] ids = {R.styleable.CyborgView_controller, R.styleable.CyborgView_tag};

	public CyborgViewSetter() {
		super(CyborgView.class, R.styleable.CyborgView, ids);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void setAttribute(CyborgView instance, TypedArray a, int attr) {
		if (attr == R.styleable.CyborgView_controller) {
			String controllerName = a.getString(attr);
			if (controllerName == null || controllerName.length() == 0)
				throw new BadImplementationException("MUST specify a valid a controller class name");

			if (controllerName.startsWith("."))
				controllerName = cyborg.getPackageName() + controllerName;
			try {
				Class<? extends CyborgController> controllerType = (Class<? extends CyborgController>) getClass().getClassLoader().loadClass(controllerName);
				CyborgController controller = ReflectiveTools.newInstance(controllerType);
				instance.setController(controller);
			} catch (ClassNotFoundException e) {
				throw new BadImplementationException("MUST specify a valid controller class name, found: " + controllerName, e);
			}
			return;
		}
		if (attr == R.styleable.CyborgView_tag) {
			String xmlTag = a.getString(attr);
			instance.setStateTag(xmlTag);
		}
	}
}

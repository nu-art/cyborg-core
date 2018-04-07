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

package com.nu.art.cyborg.ui.views.valueChanger;

import android.content.res.TypedArray;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.core.CyborgStackController;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.reflection.annotations.ReflectiveInitialization;

/**
 * Setting the xml attributes onto a {@link CyborgStackController} instance.
 */
@ReflectiveInitialization
public class ValueChangerSetter
	extends AttributesSetter<ValueChanger> {

	private static int[] ids = {//
		R.styleable.ValueChanger_deltaValue,
		//
		R.styleable.ValueChanger_value,
		//
		R.styleable.ValueChanger_label
	};

	private ValueChangerSetter() {
		super(ValueChanger.class, R.styleable.ValueChanger, ids);
	}

	@Override
	protected void setAttribute(ValueChanger instance, TypedArray a, int attr) {
		if (attr == R.styleable.ValueChanger_deltaValue) {
			float minus1 = a.getFloat(attr, 0);
			instance.setDeltaValue(minus1);
			return;
		}
		if (attr == R.styleable.ValueChanger_value) {
			float value = a.getFloat(attr, 0);
			instance.setValue(value);
			return;
		}
		if (attr == R.styleable.ValueChanger_label) {
			String label = a.getString(attr);
			instance.setLabel(label);
			return;
		}
	}
}

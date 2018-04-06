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
import com.nu.art.cyborg.core.animations.PredefinedTransitions;
import com.nu.art.cyborg.core.animations.transitions.BaseTransition;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.reflection.annotations.ReflectiveInitialization;

/**
 * Setting the xml attributes onto a {@link CyborgStackController} instance.
 */
@ReflectiveInitialization
public class CyborgStackSetter
	extends AttributesSetter<CyborgStackController> {

	private static int[] ids = {//
		R.styleable.StackController_transition,
		//
		R.styleable.StackController_transitionOrientation,
		//
		R.styleable.StackController_transitionDuration,
		//
		R.styleable.StackController_popOnBackPress,
		//
		R.styleable.StackController_rootLayoutId,
		//
		R.styleable.StackController_rootSaveState,
		//
		R.styleable.StackController_rootController,
		//
		R.styleable.StackController_rootTag
	};

	private CyborgStackSetter() {
		super(CyborgStackController.class, R.styleable.StackController, ids);
	}

	@Override
	protected void setAttribute(CyborgStackController instance, TypedArray a, int attr) {
		if (attr == R.styleable.StackController_transition) {
			int transition = a.getInt(attr, -1);
			if (transition == -1)
				return;

			instance.setDefaultTransition(PredefinedTransitions.values()[transition]);
			return;
		}

		if (attr == R.styleable.StackController_transitionOrientation) {
			int transitionOrientation = a.getInt(attr, BaseTransition.ORIENTATION_HORIZONTAL);
			instance.setDefaultTransitionOrientation(transitionOrientation);
			return;
		}

		if (attr == R.styleable.StackController_transitionDuration) {
			int duration = a.getInt(attr, -1);
			if (duration == -1)
				return;

			instance.setTransitionDuration(duration);
			return;
		}

		if (attr == R.styleable.StackController_rootSaveState) {
			boolean saveState = a.getBoolean(attr, true);
			instance.setRootSaveState(saveState);
			return;
		}

		if (attr == R.styleable.StackController_rootLayoutId) {
			int layoutId = a.getResourceId(attr, -1);
			instance.setRootLayoutId(layoutId);
			return;
		}

		if (attr == R.styleable.StackController_rootController) {
			String controllerName = a.getString(attr);
			if (controllerName == null)
				return;

			Class<? extends CyborgController> rootControllerType = resolveClassType(CyborgController.class, controllerName);
			instance.setRootControllerType(rootControllerType);
			return;
		}

		if (attr == R.styleable.StackController_rootTag) {
			String rootTag = a.getString(attr);
			instance.setRootTag(rootTag);
			return;
		}

		if (attr == R.styleable.StackController_popOnBackPress) {
			boolean popOnBackPress = a.getBoolean(attr, true);
			instance.setPopOnBackPress(popOnBackPress);
		}
	}
}

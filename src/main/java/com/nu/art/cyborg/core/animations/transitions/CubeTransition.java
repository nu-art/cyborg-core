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

package com.nu.art.cyborg.core.animations.transitions;

import android.view.View;

public class CubeTransition
	extends BaseTransition {

	public CubeTransition() {}

	@Override
	protected void applyTransform(float interpolatedTime, View view) {
		float rotationAngle = 90f * interpolatedTime;
		if (orientation == ORIENTATION_HORIZONTAL) {
			view.setTranslationX(parentWidth * interpolatedTime);
			view.setPivotX(type == TYPE_TARGET ? 0 : width);
			view.setPivotY(height * 0.5f);
			view.setRotationY(rotationAngle);
		} else {
			view.setTranslationY(-parentHeight * interpolatedTime);
			view.setPivotY(type == TYPE_TARGET ? height : 0);
			view.setPivotX(width * 0.5f);
			view.setRotationX(rotationAngle);
		}
	}
}
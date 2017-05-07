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

package com.nu.art.cyborg.core.animations;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.nu.art.software.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.core.CyborgStackController.StackLayer;
import com.nu.art.cyborg.core.CyborgStackController.StackTransitionAnimator;
import com.nu.art.cyborg.core.animations.transitions.BaseTransition;
import com.nu.art.cyborg.core.animations.transitions.BaseTransition.BaseTransitionHelper;
import com.nu.art.cyborg.ui.animations.interpulator.ReverseInterpolator;

/**
 * Created by TacB0sS on 19-Jul 2015.
 */
public class PredefinedStackTransitionAnimator
		implements StackTransitionAnimator {

	private Animation outOrigin;

	private Animation inOrigin;

	private Animation outTarget;

	private Animation inTarget;

	public PredefinedStackTransitionAnimator(Context context, BaseTransitionHelper transitionHelper, int orientation) {
		this(context, transitionHelper, orientation, false);
	}

	public PredefinedStackTransitionAnimator(Context context, BaseTransitionHelper transitionHelper, int orientation, boolean reverse) {
		if (orientation != BaseTransition.ORIENTATION_HORIZONTAL && orientation != BaseTransition.ORIENTATION_VERTICAL)
			throw new BadImplementationException("wrong orientation type... = " + orientation);

		outTarget = transitionHelper.getTargetAnimationInstance(context, orientation, reverse);
		inOrigin = transitionHelper.getOriginAnimationInstance(context, orientation, reverse);

		inTarget = transitionHelper.getTargetAnimationInstance(context, orientation, reverse);
		outOrigin = transitionHelper.getOriginAnimationInstance(context, orientation, reverse);

		ReverseInterpolator reverseInterpolator = new ReverseInterpolator();
		if (outTarget != null)
			outTarget.setInterpolator(reverseInterpolator);
		if (inOrigin != null)
			inOrigin.setInterpolator(reverseInterpolator);
	}

	@Override
	public void animateIn(StackLayer originLayer, StackLayer targetLayer, int duration, AnimationListener listener) {
		if (originLayer != null)
			animateLayer(outOrigin, originLayer.getRootView(), duration, null);

		animateLayer(inTarget, targetLayer.getRootView(), duration, listener);
	}

	@Override
	public void animateOut(StackLayer originLayer, StackLayer targetLayer, int duration, AnimationListener listener) {
		animateLayer(outTarget, targetLayer.getRootView(), duration, listener);

		if (originLayer != null)
			animateLayer(inOrigin, originLayer.getRootView(), duration, null);
	}

	private void animateLayer(Animation animation, View view, int duration, AnimationListener listener) {
		if (animation == null)
			return;

		animation.setDuration(duration);
		if (listener != null)
			animation.setAnimationListener(listener);

		view.startAnimation(animation);
	}
}

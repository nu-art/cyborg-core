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
import android.view.animation.AnimationUtils;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.core.animations.transitions.BaseTransition.BaseTransitionHelper;
import com.nu.art.cyborg.core.animations.transitions.CubeTransition;
import com.nu.art.cyborg.core.animations.transitions.ScaleTransition;
import com.nu.art.cyborg.core.animations.transitions.SlideTransition;
import com.nu.art.cyborg.core.animations.transitions.SquashTransition;
import com.nu.art.cyborg.core.animations.transitions.BaseTransition;
import com.nu.art.reflection.tools.ReflectiveTools;

/**
 * Created by TacB0sS on 24-Jul 2015.
 */
public enum PredefinedTransitions
		implements BaseTransitionHelper {
	Slide(SlideTransition.class),
	Squash(SquashTransition.class),
	Scale(ScaleTransition.class),
	Cube(CubeTransition.class),
	Fade(R.anim.push_fade_in, R.anim.pop_fade_out),
	None(null),//
	;

	private final int targetAnimation;

	private final int originAnimation;

	private final Class<? extends BaseTransition> animationType;

	PredefinedTransitions(int targetAnimation, int originAnimation) {
		this(targetAnimation, originAnimation, null);
	}

	PredefinedTransitions(Class<? extends BaseTransition> animationType) {
		this(-1, -1, animationType);
	}

	PredefinedTransitions(int targetAnimation, int originAnimation, Class<? extends BaseTransition> animationType) {
		this.targetAnimation = targetAnimation;
		this.originAnimation = originAnimation;
		this.animationType = animationType;
	}

	public Animation getTargetAnimationInstance(Context context, int orientation, boolean reverse) {
		if (animationType != null) {
			BaseTransition animation = ReflectiveTools.newInstance(animationType);
			animation.setReverse(reverse);
			animation.setOrientation(orientation);
			animation.setType(!reverse ? BaseTransition.TYPE_TARGET : BaseTransition.TYPE_ORIGIN);
			animation.setView(new View(context));
			return animation;
		}

		if (targetAnimation == -1)
			return new Animation() {};

		return AnimationUtils.loadAnimation(context, targetAnimation);
	}

	public Animation getOriginAnimationInstance(Context context, int orientation, boolean reverse) {
		if (animationType != null) {
			BaseTransition animation = ReflectiveTools.newInstance(animationType);
			animation.setReverse(reverse);
			animation.setOrientation(orientation);

			animation.setType(!reverse ? BaseTransition.TYPE_ORIGIN : BaseTransition.TYPE_TARGET);
			animation.setView(new View(context));
			return animation;
		}

		if (targetAnimation == -1)
			return new Animation() {};

		return AnimationUtils.loadAnimation(context, originAnimation);
	}
}

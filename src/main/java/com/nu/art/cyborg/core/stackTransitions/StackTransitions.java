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

package com.nu.art.cyborg.core.stackTransitions;

import com.nu.art.cyborg.core.CyborgStackController.StackLayerBuilder;
import com.nu.art.reflection.tools.ReflectiveTools;

/**
 * Created by TacB0sS on 24-Jul 2015.
 */
public enum StackTransitions
	implements Transition {

	SlideL2R(Transition_SlideHorizontal.class),
	SlideR2L(SlideL2R, true),
	Slide(SlideL2R),
	SlideT2B(Transition_SlideVertical.class),
	SlideB2T(SlideT2B, true),

	ScaleL2R(Transition_ScaleHorizontal.class),
	ScaleR2L(ScaleL2R, true),
	Scale(ScaleL2R),
	ScaleT2B(Transition_ScaleVertical.class),
	ScaleB2T(ScaleT2B, true),

	CubeL2R(Transition_CubeHorizontal.class),
	CubeR2L(CubeL2R, true),
	Cube(CubeL2R),
	CubeT2B(Transition_CubeVertical.class),
	CubeB2T(CubeT2B, true),

	Fade(Transition_Fade.class),
	;

	private final Transition transition;
	private final boolean reverse;

	StackTransitions(Class<? extends Transition> transitionType) {
		this(ReflectiveTools.newInstance(transitionType));
	}

	StackTransitions(Transition transition) {
		this(transition, false);
	}

	StackTransitions(Transition transition, boolean reverse) {
		this.transition = transition;
		this.reverse = reverse;
	}

	@Override
	public void animate(StackLayerBuilder layer, float progress, boolean in) {
		if (reverse) {
			in = !in;
			progress = 1 - progress;
		}

		transition.animate(layer, progress, in);
	}

	@Override
	public Transition reverse() {
		return SlideL2R;
	}

}

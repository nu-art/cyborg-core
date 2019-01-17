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

package com.nu.art.cyborg.core.animations.transitions;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by TacB0sS on 27-Jul 2015.
 */
public abstract class BaseTransition
	extends Animation {

	@Documented
	@Retention(SOURCE)
	@Target( {
		         PARAMETER,
		         FIELD,
		         LOCAL_VARIABLE
	         })
	@IntDef( {
		         ORIENTATION_HORIZONTAL,
		         ORIENTATION_VERTICAL
	         })
	public @interface TransitionOrientation {}

	public interface BaseTransitionHelper {

		Animation getTargetAnimationInstance(Context context, int orientation, boolean reverse);

		Animation getOriginAnimationInstance(Context context, int orientation, boolean reverse);
	}

	public static final int ORIENTATION_HORIZONTAL = 0;

	public static final int ORIENTATION_VERTICAL = 1;

	public static final int TYPE_ORIGIN = 2;

	public static final int TYPE_TARGET = 3;

	protected boolean reverse;

	protected float width;

	protected float height;

	protected int parentWidth;

	protected int parentHeight;

	protected int orientation;

	protected int type;

	private View view;

	public final void setView(View view) {
		this.view = view;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		this.height = height;
		this.width = width;
		this.parentWidth = parentWidth;
		this.parentHeight = parentHeight;
	}

	@Override
	protected final void applyTransformation(float interpolatedTime, Transformation t) {
		float finalInterpolatedTime = calcInterpolationTime(interpolatedTime);
		applyTransform(finalInterpolatedTime, view);
		t.getMatrix().set(view.getMatrix());
	}

	protected float calcInterpolationTime(float interpolatedTime) {
		switch (type) {
			case TYPE_TARGET:
				if (reverse)
					return interpolatedTime;
				else
					return 1 - interpolatedTime;
			case TYPE_ORIGIN:
				if (reverse)
					return 0 - (1 - interpolatedTime);
				else
					return 0 - interpolatedTime;
		}
		return interpolatedTime;
	}

	protected abstract void applyTransform(float interpolatedTime, View view);
}

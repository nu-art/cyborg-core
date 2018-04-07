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

package com.nu.art.cyborg.ui.animations;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

public class ScaleAnimation
	extends Animation {

	public enum Horizontal {
		Right,
		Left
	}

	public enum Vertical {
		Top,
		Bottom
	}

	private View view;

	private int startX;

	private int startY;

	private Vertical vertical;

	private LayoutParams mLayoutParams;

	private int dx;

	private int dy;

	public ScaleAnimation(View view, int duration) {
		setDuration(duration);
		// setFillBefore(false);
		// setFillEnabled(true);
		// setFillAfter(true);
		this.view = view;
		mLayoutParams = view.getLayoutParams();
		startX = mLayoutParams.width;
		startY = mLayoutParams.height;
	}

	/*/**
	 * @param unit   one of the {@link android.util.TypedValue} with one of the complex units
     * @param startX
     * @param endX
     * @return
     */
	public final ScaleAnimation scaleX(int unit, int startX, int endX) {
		startX = (int) TypedValue.applyDimension(unit, startX, view.getResources().getDisplayMetrics());
		endX = (int) TypedValue.applyDimension(unit, endX, view.getResources().getDisplayMetrics());

		this.startX = startX;
		this.dx = endX - startX;
		return this;
	}

	/*/**
	 * @param unit one of the {@link android.util.TypedValue} with one of the complex units
     * @param endY
     * @return this.
     */
	public final ScaleAnimation scaleY(int unit, int endY) {
		return scaleY(unit, Vertical.Top, startY, endY);
	}

	/*/**
	 * @param unit   one of the {@link android.util.TypedValue} with one of the complex units
     * @param startY
     * @param endY
     * @return this.
     */
	public final ScaleAnimation scaleY(int unit, Vertical vertical, int startY, int endY) {
		startY = (int) TypedValue.applyDimension(unit, startY, view.getResources().getDisplayMetrics());
		endY = (int) TypedValue.applyDimension(unit, endY, view.getResources().getDisplayMetrics());
		this.vertical = vertical;
		this.startY = startY;
		this.dy = endY - startY;
		return this;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		mLayoutParams.height = startY + (int) (dy * interpolatedTime);
		mLayoutParams.width = startX + (int) (dx * interpolatedTime);
		if (vertical == Vertical.Bottom && mLayoutParams instanceof RelativeLayout.LayoutParams) {
			((RelativeLayout.LayoutParams) mLayoutParams).topMargin = -dy;
		}
		view.requestLayout();
	}

	public void execute(AnimationListener animationListener) {
		setAnimationListener(animationListener);
		view.setAnimation(this);
		view.startAnimation(this);
	}
}

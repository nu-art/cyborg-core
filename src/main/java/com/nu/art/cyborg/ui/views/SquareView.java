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

package com.nu.art.cyborg.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.nu.art.core.exceptions.runtime.ImplementationMissingException;
import com.nu.art.cyborg.R;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.reflection.annotations.ReflectiveInitialization;

public class SquareView
	extends FrameLayout {

	private int aspectRatioWidth;

	private int aspectRatioHeight;

	public SquareView(Context context) {
		this(context, null);
	}

	public SquareView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SquareView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		CyborgBuilder.handleAttributes(this, context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
		int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

		// Consider this might not be good for
		int calculatedHeight = originalWidth * aspectRatioHeight / aspectRatioWidth;

		int finalWidth, finalHeight;

		finalWidth = originalWidth;
		finalHeight = calculatedHeight;

		super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
	}

	@ReflectiveInitialization
	public static class SquareViewSetter
		extends AttributesSetter<SquareView> {

		private static int[] ids = {
			R.styleable.SquareView_heightRatio,
			R.styleable.SquareView_widthRatio
		};

		private SquareViewSetter() {
			super(SquareView.class, R.styleable.SquareView, ids);
		}

		@Override
		protected void setAttribute(SquareView instance, TypedArray a, int attr) {
			if (attr == R.styleable.SquareView_heightRatio) {
				instance.aspectRatioHeight = a.getInt(attr, -1);
				if (instance.aspectRatioHeight == -1)
					throw new ImplementationMissingException("MUST set value for app:heightRatio");
				return;
			}

			if (attr == R.styleable.SquareView_widthRatio) {
				instance.aspectRatioWidth = a.getInt(attr, -1);
				if (instance.aspectRatioWidth == -1)
					throw new ImplementationMissingException("MUST set value for app:widthRatio");
				return;
			}
		}
	}
}

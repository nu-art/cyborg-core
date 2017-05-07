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

package com.nu.art.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class CountCircleView
		extends View {

	private static final int DefaultHeightMargin = 6;

	private static final int DefaultWidthMargin = 10;

	private Paint labelPaint;

	private Rect labelRect;

	private int width;

	private int height;

	private int count;

	private int marginHeight = DefaultHeightMargin;

	private int marginWidth = DefaultWidthMargin;

	private int maxTextSize = 32;

	public CountCircleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CountCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CountCircleView(Context context) {
		super(context);
		init();
	}

	private final void init() {
		labelPaint = new Paint();
		labelRect = new Rect();
		labelPaint.setColor(Color.WHITE);
		labelPaint.setTextSize(28);
		labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
		setMaxTextSize_SP(maxTextSize);

		marginHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DefaultHeightMargin, getResources().getDisplayMetrics());
		marginWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DefaultWidthMargin, getResources().getDisplayMetrics());
	}

	public final void setCount(int count) {
		if (this.count != count) {
			labelPaint.setTextSize(maxTextSize);
			invalidate();
		}
		this.count = count;
	}

	public void setMarginHeight(int marginHeight) {
		this.marginHeight = marginHeight + DefaultHeightMargin;
	}

	public void setMarginWidth(int marginWidth) {
		this.marginWidth = marginWidth + DefaultWidthMargin;
	}

	public void setMaxTextSize_SP(int maxTextSize) {
		this.maxTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maxTextSize, getResources().getDisplayMetrics());
	}

	public void setValueColor(int color) {
		labelPaint.setColor(color);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void draw(Canvas canvas) {
		if (isInEditMode()) {
			super.draw(canvas);
			return;
		}
		if (count == 0)
			return;
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		width = getWidth();
		height = getHeight();

		super.draw(canvas);
		drawLabel(canvas);
	}

	public final void drawLabel(Canvas canvas) {
		String label = "";
		if (count > 9)
			label = "9+";
		else
			label = "" + count;

		float[] fs = new float[2];
		while (true) {
			labelPaint.getTextWidths(label, fs);
			labelPaint.getTextBounds(label, 0, label.length(), labelRect);
			if (labelRect.bottom - labelRect.top < height - marginHeight && labelRect.right - labelRect.left < width - marginWidth)
				break;

			if (labelPaint.getTextSize() <= 5) {
				labelPaint.setTextSize(10);
				break;
			}

			labelPaint.setTextSize(labelPaint.getTextSize() - 1);
		}
		float finalX = (width - (fs[0] + fs[1])) / 2f;
		float finalY = (height - (labelRect.top - labelRect.bottom)) / 2f;
		canvas.drawText(label, finalX, finalY, labelPaint);
	}
}

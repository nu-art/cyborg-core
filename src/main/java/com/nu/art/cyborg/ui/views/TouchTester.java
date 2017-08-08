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

package com.nu.art.cyborg.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class TouchTester
		extends View {

	private ArrayList<Point> points = new ArrayList<>();

	private Paint paint = new Paint();

	public TouchTester(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TouchTester(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TouchTester(Context context) {
		super(context);
		init();
	}

	private void init() {
		paint.setColor(Color.RED);
	}

	public final void addPoint(Point point) {
		synchronized (points) {
			points.add(point);
		}
		invalidate();
	}

	public final void removePoint(Point point) {
		synchronized (points) {
			points.remove(point);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		synchronized (points) {
			for (Point point : points) {
				canvas.drawCircle(point.x, point.y, 10, paint);
			}
		}
	}
}

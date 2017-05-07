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

package com.nu.art.cyborg.common.utils;

import android.graphics.Rect;
import android.view.View;

@SuppressWarnings("WeakerAccess")
public final class Tools {

	public static int getViewRealLeft(View view) {
		int left = 0;
		while (view.getId() != android.R.id.content) {
			left += view.getLeft();
			view = (View) view.getParent();
		}
		return left;
	}

	public static int getViewRealTop(View view) {
		int top = 0;
		while (view.getId() != android.R.id.content) {
			top += view.getTop();
			view = (View) view.getParent();
		}
		return top;
	}

	public static Rect getViewRealRect(View originView) {
		Rect rect = new Rect();
		originView.getGlobalVisibleRect(rect);
		int width = rect.right - rect.left;
		int height = rect.bottom - rect.top;
		rect.left = Tools.getViewRealLeft(originView);
		rect.top = Tools.getViewRealTop(originView);
		rect.right = rect.left + width;
		rect.bottom = rect.top + height;
		return rect;
	}
}

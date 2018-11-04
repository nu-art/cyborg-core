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

package com.nu.art.cyborg.core;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.interfaces.ActivityLifeCycleImpl;

public class KeyboardChangeListener
	extends Logger {

	public interface OnKeyboardVisibilityListener {

		void onVisibilityChanged(boolean visible);
	}

	private final Activity activity;

	private final Cyborg cyborg;

	private OnGlobalLayoutListener layoutChangeListener;

	KeyboardChangeListener(Cyborg cyborg, Activity activity) {
		this.cyborg = cyborg;
		this.activity = activity;
		activity.getApplication().registerActivityLifecycleCallbacks(new ActivityLifeCycleImpl() {
			@Override
			public void onActivityResumed(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				addLayoutChangeListener();
			}

			@Override
			public void onActivityPaused(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				removeLayoutChangeListener();
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				removeLayoutChangeListener();
			}
		});
	}

	private void removeLayoutChangeListener() {
		final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
		if (activityRootView == null)
			return;

		ViewTreeObserver viewTreeObserver = activityRootView.getViewTreeObserver();
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			viewTreeObserver.removeOnGlobalLayoutListener(layoutChangeListener);
		} else
			viewTreeObserver.removeGlobalOnLayoutListener(layoutChangeListener);
	}

	private void addLayoutChangeListener() {
		final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
		if (activityRootView == null)
			return;

		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutChangeListener = new OnGlobalLayoutListener() {

			private final int DefaultKeyboardDP = 100;

			// From @nathanielwolf answer...  Lollipop includes button bar in the root. Add height of button bar (48dp) to maxDiff
			private final int EstimatedKeyboardDP = DefaultKeyboardDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);

			private final Rect r = new Rect();

			private boolean wasOpened;

			@Override
			public void onGlobalLayout() {
				int estimatedKeyboardHeight = cyborg.dpToPx(EstimatedKeyboardDP);

				activityRootView.getWindowVisibleDisplayFrame(r);
				int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
				final boolean isShown = heightDiff >= estimatedKeyboardHeight;

				if (isShown == wasOpened)
					return;

				wasOpened = isShown;
				cyborg.dispatchEvent(KeyboardChangeListener.this, "Keyboard visibility changed: " + isShown, OnKeyboardVisibilityListener.class, new Processor<OnKeyboardVisibilityListener>() {
					@Override
					public void process(OnKeyboardVisibilityListener listener) {
						listener.onVisibilityChanged(isShown);
					}
				});
			}
		});
	}
}

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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import androidx.viewpager.widget.ViewPager;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.cyborg.ui.tools.CustomScroller;
import com.nu.art.reflection.annotations.ReflectiveInitialization;

import java.lang.reflect.Field;

/**
 * Created by TacB0sS on 06-Aug 2016.
 */
public class CyborgViewPager
	extends ViewPager {

	private boolean blockUserSwiping;

	public CyborgViewPager(Context context) {
		super(context);
		init(context, null, -1);
	}

	public CyborgViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, -1);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		CyborgBuilder.getModule(isInEditMode() ? context : null, AttributeModule.class).setAttributes(context, attrs, this);
	}

	public void setBlockUserSwiping(boolean blockUserSwiping) {
		this.blockUserSwiping = blockUserSwiping;
	}

	public boolean isBlockUserSwiping() {
		return blockUserSwiping;
	}

	public void setAdapter(CyborgAdapter adapter) {
		super.setAdapter(adapter.getPagerAdapter());
	}

	public void setAutoScrollSpeed(int speedInMillis)
		throws Exception {
		Field mScroller;
		mScroller = ViewPager.class.getDeclaredField("mScroller");
		mScroller.setAccessible(true);
		CustomScroller scroller = new CustomScroller(getContext(), new DecelerateInterpolator());
		scroller.setFixedDuration(speedInMillis);
		mScroller.set(this, scroller);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// Never allow swiping to switch between pages
		return !blockUserSwiping && super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Never allow swiping to switch between pages
		return !blockUserSwiping && super.onTouchEvent(event);
	}

	/**
	 * Setting the xml attributes onto a {@link CyborgRecycler} instance.
	 */
	@ReflectiveInitialization
	public static class CyborgViewPagerSetter
		extends AttributesSetter<CyborgViewPager> {

		private static int[] ids = {
			R.styleable.ViewPager_scrollIntervalMS,
			R.styleable.ViewPager_blockUserSwiping,
		};

		private CyborgViewPagerSetter() {
			super(CyborgViewPager.class, R.styleable.ViewPager, ids);
		}

		@Override
		protected void setAttribute(CyborgViewPager instance, TypedArray a, int attr) {
			if (attr == R.styleable.ViewPager_scrollIntervalMS) {
				int scrollIntervalMS = a.getInt(attr, 500);
				try {
					instance.setAutoScrollSpeed(scrollIntervalMS);
				} catch (Exception e) {
					logError("", e);
				}
			}

			if (attr == R.styleable.ViewPager_blockUserSwiping) {
				boolean disableSwipe = a.getBoolean(attr, false);
				try {
					instance.setBlockUserSwiping(disableSwipe);
				} catch (Exception e) {
					logError("", e);
				}
			}
		}
	}
}

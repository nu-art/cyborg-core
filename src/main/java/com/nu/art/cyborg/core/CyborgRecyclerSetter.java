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

package com.nu.art.cyborg.core;

import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.reflection.annotations.ReflectiveInitialization;

/**
 * Setting the xml attributes onto a {@link CyborgRecycler} instance.
 */
@ReflectiveInitialization
public class CyborgRecyclerSetter
		extends AttributesSetter<CyborgRecycler> {

	private static int[] ids = {
			R.styleable.Recycler_orientation,
			R.styleable.Recycler_horizontalSpacing,
			R.styleable.Recycler_verticalSpacing,
			R.styleable.Recycler_landscapeColumnsCount,
			R.styleable.Recycler_portraitColumnsCount
	};

	private CyborgRecyclerSetter() {
		super(CyborgRecycler.class, R.styleable.Recycler, ids);
	}

	@Override
	protected void setAttribute(CyborgRecycler instance, TypedArray a, int attr) {
		if (attr == R.styleable.Recycler_orientation) {
			int margin = a.getInt(attr, 0);
			instance.setLayoutOrientation(margin == 0 ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
			return;
		}
		if (attr == R.styleable.Recycler_horizontalSpacing) {
			int horizontalSpacing = a.getDimensionPixelSize(attr, 0);
			instance.setHorizontalSpacing(horizontalSpacing);
			return;
		}
		if (attr == R.styleable.Recycler_verticalSpacing) {
			int verticalSpacing = a.getDimensionPixelSize(attr, 0);
			instance.setVerticalSpacing(verticalSpacing);
			return;
		}
		if (attr == R.styleable.Recycler_landscapeColumnsCount) {
			int columnsCount = a.getInt(attr, 1);
			instance.setLandscapeColumnsCount(columnsCount);
			return;
		}
		if (attr == R.styleable.Recycler_portraitColumnsCount) {
			int columnsCount = a.getInt(attr, 1);
			instance.setPortraitColumnsCount(columnsCount);
		}
	}

	@Override
	protected void onSettingCompleted(CyborgRecycler instance) {
		instance.invalidateLayoutManager();
	}
}

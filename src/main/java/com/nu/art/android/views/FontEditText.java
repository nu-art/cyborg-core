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
import android.util.AttributeSet;
import android.widget.EditText;

import com.nu.art.cyborg.core.CyborgBuilder;

public final class FontEditText
	extends EditText {

	public FontEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		CyborgBuilder.handleAttributes(this, context, attrs);
	}

	public FontEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		CyborgBuilder.handleAttributes(this, context, attrs);
	}
}

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

package com.nu.art.cyborg.modules.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.R;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;

public class FontTypeSetter
		extends AttributesSetter<TextView> {

	public interface SupportedFont {

		String attributeName();

		Typeface getTypeface(Context application);
	}

	private float spFactor;

	private SupportedFont[] fonts = {};

	public FontTypeSetter() {
		super(TextView.class, R.styleable.CustomFont, R.styleable.CustomFont_font);
	}

	public void addFonts(SupportedFont... fonts) {
		this.fonts = ArrayTools.appendElements(this.fonts, fonts);
	}

	protected void init() {
		spFactor = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1, cyborg.getResources().getDisplayMetrics());
	}

	@Override
	protected void setAttribute(TextView textview, TypedArray a, int attr) {
		String fontName = a.getString(attr);
		if (fontName == null) {
			logWarning("No attribute set: " + attr);
			return;
		}

		SupportedFont font = getFontsValues(fontName);
		if (font == null) {
			logWarning("Could not find a font with a matching name: " + fontName);
			return;
		}
		Typeface typeface = font.getTypeface(textview.getContext());
		textview.setTypeface(typeface);
	}

	/**
	 * Keeps the app look unified cross font size changes of users
	 */
	protected void scaleFontType(TextView view) {
		float scale = view.getResources().getConfiguration().fontScale;
		float size = view.getTextSize();
		float finalFontSize = size / scale / spFactor;
		view.setTextSize(finalFontSize);
	}

	private SupportedFont getFontsValues(String fontName) {
		for (SupportedFont supportedFont : fonts) {
			if (!supportedFont.attributeName().equals(fontName))
				continue;
			return supportedFont;
		}
		return null;
	}
}

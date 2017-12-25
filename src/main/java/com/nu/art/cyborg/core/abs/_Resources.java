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

package com.nu.art.cyborg.core.abs;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public interface _Resources {

	/**
	 * Numeric Ids.
	 */
	int[] numericIds = {
			R.string._0,
			R.string._1,
			R.string._2,
			R.string._3,
			R.string._4,
			R.string._5,
			R.string._6,
			R.string._7,
			R.string._8,
			R.string._9
	};

	/**
	 * @return The Application object of your app.
	 */
	Context getApplicationContext();

	/**
	 * @return The package name of your app.
	 */
	String getPackageName();

	/**
	 * @param stringId The string id to extract.
	 * @param params   The params for the string format.
	 *
	 * @return a composed string after formatting.
	 */
	String getString(int stringId, Object... params);

	/**
	 * @param stringResolver The string resolver to be resolved.
	 *
	 * @return the resolved string.
	 */
	String getString(StringResourceResolver stringResolver);

	/**
	 * @param numericString the numeric string to replace.
	 *
	 * @return The numeric string in current locale
	 */
	String convertNumericString(String numericString);

	/**
	 * Utility function to convert dp to px
	 *
	 * @param dp the value of dp to convert.
	 *
	 * @return the provided dp value in pixels
	 */
	int dpToPx(int dp);

	/**
	 * @return Android locale {@link Resources}
	 */
	Resources getResources();

	/**
	 * @param resourceId The desired resource id.
	 *
	 * @return Resource input stream of the provided resource id.
	 */
	InputStream getRawResources(int resourceId);

	/**
	 * @param assetName The desired asset's Name.
	 *
	 * @return Asset input stream of the provided assetName.
	 *
	 * @throws IOException if something goes wrong...
	 */
	InputStream getAsset(String assetName)
			throws IOException;

	/**
	 * @return Android's {@link ContentResolver}.
	 */
	ContentResolver getContentResolver();

	/**
	 * @return The current Locale.
	 */
	Locale getLocale();

	/**
	 * @param dimensionId The desired dimension id.
	 *
	 * @return The value of the provided dimension id.
	 */
	float getDimension(int dimensionId);

	/**
	 * @param colorId The desired color id.
	 *
	 * @return The value of the provided color id.
	 */
	int getColor(int colorId);

	/**
	 * @param type The type of provided dimension.
	 * @param size The value of the dimension
	 *
	 * @return the size in pixels.
	 */
	float dimToPx(int type, float size);
}

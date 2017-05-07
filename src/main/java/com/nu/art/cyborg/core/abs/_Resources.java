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
import android.content.res.Resources;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.common.interfaces.StringResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public interface _Resources {

	int[] numericIds = {R.string._0,
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

	Application getApplication();

	String getPackageName();

	String getString(int stringId, Object... params);

	String getString(StringResourceResolver stringResolver);

	String convertNumericString(String numericString);

	int dpToPx(int dp);

	Resources getResources();

	InputStream getRawResources(int resourceId);

	InputStream getAsset(String assetName)
			throws IOException;

	ContentResolver getContentResolver();

	Locale getLocale();

	float getDimension(int dimensionId);

	int getColor(int colorId);

	float getDynamicDimension(int type, float size);
}

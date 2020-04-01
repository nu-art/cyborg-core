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

package com.nu.art.cyborg.modules.downloader.converters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nu.art.core.exceptions.runtime.ThisShouldNotHappenException;

import java.io.InputStream;

@SuppressWarnings("unused")
public class Converter_WithReusableBitmap
	implements Converter_Bitmap {

	private BitmapFactory.Options options;

	/**
	 * @param inBitmap, the bitmap object to override with the new image. Can be null.
	 */
	public Converter_WithReusableBitmap(Bitmap inBitmap) {
		options = new BitmapFactory.Options();
		options.inMutable = true;
		options.inSampleSize = 1;
		options.inBitmap = inBitmap;
	}

	/**
	 * @param inputStream the stream of bytes of the image.
	 *
	 * @return a new bitmap object if inBitmap was null, or the inBitmap provided with new content.
	 */
	@Override
	public Bitmap map(InputStream inputStream) {
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
		if (bitmap == null)
			throw new ThisShouldNotHappenException("Could not create bitmap from input stream");

		return bitmap;
	}
}

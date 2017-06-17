package com.nu.art.cyborg.modules.downloader.converters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nu.art.core.generics.Function;

import java.io.InputStream;

/**
 * Created by tacb0ss on 14/06/2017.
 */

@SuppressWarnings("unused")
public class Converter_Bitmap
		implements Function<InputStream, Bitmap> {

	public static final Converter_Bitmap converter = new Converter_Bitmap();

	@Override
	public Bitmap map(InputStream inputStream) {
		return BitmapFactory.decodeStream(inputStream);
	}
}

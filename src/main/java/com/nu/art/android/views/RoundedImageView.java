package com.nu.art.android.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by TacB0sS on 14-Oct 2017.
 */

public class RoundedImageView
		extends ImageView {

	public RoundedImageView(Context context) {
		super(context);
	}

	public RoundedImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@RequiresApi(api = VERSION_CODES.LOLLIPOP)
	public RoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void setRoundedImage(Uri photoUri) {

		if (photoUri == null)
			return;

		InputStream inputStream = null;
		try {
			inputStream = getContext().getContentResolver().openInputStream(photoUri);
			Bitmap bitmapSquare = BitmapFactory.decodeStream(inputStream);
			setRoundedBitmapDrawable(bitmapSquare);
		} catch (FileNotFoundException e) {
			Log.e("RoundedImageView", "photo does not exists", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				Log.e("RoundedImageView", "Error closing inputstream", e);
			}
		}
	}

	private void setRoundedBitmapDrawable(Bitmap bitmapSquare) {
		int original_width = bitmapSquare.getWidth();
		int original_height = bitmapSquare.getHeight();
		int square_dimension = Math.min(original_width, original_height);

		if (original_height != original_width) {
			int x = (original_width - square_dimension) / 2;
			int y = (original_height - square_dimension) / 2;
			bitmapSquare = Bitmap.createBitmap(bitmapSquare, x, y, square_dimension, square_dimension);
		}

		RoundedBitmapDrawable roundedImage = RoundedBitmapDrawableFactory.create(getResources(), bitmapSquare);
		roundedImage.setCornerRadius(Math.max(original_width, original_height) / 2.0f);
		setImageDrawable(roundedImage);
	}

	public void setRoundedImage(@DrawableRes int defaultImageId) {
		setRoundedBitmapDrawable(BitmapFactory.decodeResource(getResources(), defaultImageId));
	}
}

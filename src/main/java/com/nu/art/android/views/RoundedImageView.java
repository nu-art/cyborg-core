package com.nu.art.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.R;
import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.cyborg.modules.ImageUtilsModule;
import com.nu.art.reflection.annotations.ReflectiveInitialization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by TacB0sS on 14-Oct 2017.
 */

public class RoundedImageView
	extends ImageView
	implements ILogger {

	private final ILogger logger = CyborgBuilder.getInstance().getLogger(getClass().getSimpleName());
	private int corners;
	private Bitmap src;

	public RoundedImageView(Context context) {
		super(context);
		init(context, null);
	}

	public RoundedImageView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@RequiresApi(api = VERSION_CODES.LOLLIPOP)
	public RoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (attrs == null)
			return;

		CyborgBuilder.getModule(isInEditMode() ? context : null, AttributeModule.class).setAttributes(context, attrs, this);
	}

	public void setImage(@DrawableRes int defaultImageId) {
		setImage(BitmapFactory.decodeResource(getResources(), defaultImageId));
	}

	public void setImage(Bitmap src) {
		this.src = src;
		updateImage();
	}

	public void setImage(Drawable src) {
		setImage(ImageUtilsModule.drawableToBitmap(src));
	}

	private void updateImage() {
		setRoundedBitmapDrawable(src);
	}

	public void setCornersPixels(int cornersInPixels) {
		this.corners = cornersInPixels;
		updateImage();
	}

	@Deprecated
	public void setRoundedImage(Uri photoUri) {

		if (photoUri == null)
			return;

		InputStream inputStream = null;
		try {
			inputStream = getContext().getContentResolver().openInputStream(photoUri);
			Bitmap bitmapSquare = BitmapFactory.decodeStream(inputStream);
			setImage(bitmapSquare);
		} catch (FileNotFoundException e) {
			logError("photo does not exists", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				logError("Error closing input stream", e);
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
		roundedImage.setCornerRadius(corners == -1 ? Math.max(original_width, original_height) / 2.0f : corners);
		roundedImage.setAntiAlias(true);
		setImageDrawable(roundedImage);
	}

	@ReflectiveInitialization
	public static class RoundedImageViewSetter
		extends AttributesSetter<RoundedImageView> {

		private static int[] ids = {
			R.styleable.RoundedImageView_src,
			R.styleable.RoundedImageView_corners,
		};

		private RoundedImageViewSetter() {
			super(RoundedImageView.class, R.styleable.RoundedImageView, ids);
		}

		@Override
		protected void setAttribute(RoundedImageView instance, TypedArray a, int attr) {
			if (attr == R.styleable.RoundedImageView_corners) {
				int corners = a.getDimensionPixelSize(attr, -1);
				instance.setCornersPixels(corners);
				return;
			}
			if (attr == R.styleable.RoundedImageView_src) {
				Drawable src = a.getDrawable(attr);
				instance.setImage(src);
			}
		}

		@Override
		protected void onSettingCompleted(RoundedImageView instance) {
			instance.invalidate();
		}
	}

	@Override
	public void logVerbose(String verbose) {
		if (logger != null)
			logger.logVerbose(verbose);
	}

	@Override
	public void logVerbose(String verbose, Object... params) {
		if (logger != null)
			logger.logVerbose(verbose, params);
	}

	@Override
	public void logVerbose(Throwable e) {
		if (logger != null)
			logger.logVerbose(e);
	}

	@Override
	public void logVerbose(String verbose, Throwable e) {
		if (logger != null)
			logger.logVerbose(verbose, e);
	}

	@Override
	public void logDebug(String debug) {
		if (logger != null)
			logger.logDebug(debug);
	}

	@Override
	public void logDebug(String debug, Object... params) {
		if (logger != null)
			logger.logDebug(debug, params);
	}

	@Override
	public void logDebug(Throwable e) {
		if (logger != null)
			logger.logDebug(e);
	}

	@Override
	public void logDebug(String debug, Throwable e) {
		if (logger != null)
			logger.logDebug(debug, e);
	}

	@Override
	public void logInfo(String info) {
		if (logger != null)
			logger.logInfo(info);
	}

	@Override
	public void logInfo(String info, Object... params) {
		if (logger != null)
			logger.logInfo(info, params);
	}

	@Override
	public void logInfo(Throwable e) {
		if (logger != null)
			logger.logInfo(e);
	}

	@Override
	public void logInfo(String info, Throwable e) {
		if (logger != null)
			logger.logInfo(info, e);
	}

	@Override
	public void logWarning(String warning) {
		if (logger != null)
			logger.logWarning(warning);
	}

	@Override
	public void logWarning(String warning, Object... params) {
		if (logger != null)
			logger.logWarning(warning, params);
	}

	@Override
	public void logWarning(Throwable e) {
		if (logger != null)
			logger.logWarning(e);
	}

	@Override
	public void logWarning(String warning, Throwable e) {
		if (logger != null)
			logger.logWarning(warning, e);
	}

	@Override
	public void logError(String error) {
		if (logger != null)
			logger.logError(error);
	}

	@Override
	public void logError(String error, Object... params) {
		if (logger != null)
			logger.logError(error, params);
	}

	@Override
	public void logError(Throwable e) {
		if (logger != null)
			logger.logError(e);
	}

	@Override
	public void logError(String error, Throwable e) {
		if (logger != null)
			logger.logError(error, e);
	}
}

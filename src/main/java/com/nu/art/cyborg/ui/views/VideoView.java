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

package com.nu.art.cyborg.ui.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import com.nu.art.belog.BeLogged;
import com.nu.art.belog.Logger;
import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.ILogger;
import com.nu.art.cyborg.media.CyborgMediaPlayer;

public class VideoView
	extends TextureView
	implements SurfaceTextureListener, ILogger {

	private final Logger logger = BeLogged.getInstance().getLogger(this);

	private CyborgMediaPlayer mediaPlayer;
	private Surface surface;
	private Processor<Surface> onSurfaceReadyListener;

	public VideoView(Context context) {
		super(context);
		setSurfaceTextureListener(this);
	}

	public VideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSurfaceTextureListener(this);
	}

	public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setSurfaceTextureListener(this);
	}

	public void setMediaPlayer(CyborgMediaPlayer mMediaPlayer) {
		this.mediaPlayer = mMediaPlayer;

		if (surface != null) {
			this.mediaPlayer.setSurface(surface);
		}
	}

	public void adjustAspectRatio(int videoWidth, int videoHeight) {
		if (videoWidth == 0 || videoHeight == 0)
			return;

		int viewWidth = getWidth();
		int viewHeight = getHeight();
		double aspectRatio = (double) videoHeight / videoWidth;

		int newWidth, newHeight;
		if (viewHeight > (int) (viewWidth * aspectRatio)) {
			// limited by narrow width; restrict height
			newWidth = viewWidth;
			newHeight = (int) (viewWidth * aspectRatio);
		} else {
			// limited by short height; restrict width
			newWidth = (int) (viewHeight / aspectRatio);
			newHeight = viewHeight;
		}
		int xOff = (viewWidth - newWidth) / 2;
		int yOff = (viewHeight - newHeight) / 2;

		Matrix matrix = new Matrix();
		getTransform(matrix);
		matrix.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
		matrix.postTranslate(xOff, yOff);
		setTransform(matrix);
	}

	public void setOnSurfaceReadyListener(Processor<Surface> onSurfaceReadyListener) {
		this.onSurfaceReadyListener = onSurfaceReadyListener;
		if (this.surface != null && onSurfaceReadyListener != null)
			onSurfaceReadyListener.process(this.surface);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		this.surface = new Surface(surface);
		logInfo("onSurfaceTextureAvailable");
		if (onSurfaceReadyListener != null)
			onSurfaceReadyListener.process(this.surface);

		if (mediaPlayer != null)
			mediaPlayer.setSurface(this.surface);
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		logInfo("onSurfaceTextureDestroyed");
		if (mediaPlayer != null)
			mediaPlayer.setSurface(null);
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	public Surface getSurface() {
		return surface;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mediaPlayer != null)
			mediaPlayer.dispose();
		surface.release();
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

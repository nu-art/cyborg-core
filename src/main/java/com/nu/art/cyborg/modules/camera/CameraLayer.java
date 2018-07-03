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
package com.nu.art.cyborg.modules.camera;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.nu.art.cyborg.R;
import com.nu.art.cyborg.core.CyborgController;

public final class CameraLayer
	extends CyborgController
	implements SurfaceHolder.Callback {

	private SurfaceView cameraView;
	private SurfaceHolder previewHolder = null;

	private CameraModule cameraModule;

	private int cameraId;

	private boolean startOnCreate = false;

	private int width;

	private int height;

	private boolean dispose;

	public CameraLayer() {
		super(R.layout.controller__camera_layer);
	}

	@Override
	protected void extractMembers() {
		cameraView = getViewById(R.id.SV_CameraLayer);
	}

	@Override
	protected void onCreate() {
		previewHolder = cameraView.getHolder();
		previewHolder.addCallback(this);
	}

	public final void switchToCamera(int newCameraId) {
		if (cameraId == newCameraId)
			return;

		this.cameraId = newCameraId;
		dispose = true;
		if (width <= 0 || height <= 0)
			return;

		surfaceChanged(previewHolder, -1, width, height);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		logDebug("Surface holder changed: " + holder + ", format == " + format + ", size == (" + width + ", " + height + ")");
		this.width = width;
		this.height = height;
		try {
			boolean wasStreaming = false;
			if (cameraModule.isStreaming()) {
				wasStreaming = true;
				cameraModule.stopPreview();
			}
			if (dispose) {
				dispose = false;
				cameraModule.disposeCamera();
			}

			cameraModule.configureCamera(cameraId, width, height);

			if (startOnCreate) {
				startPreview(cameraId, width, height);
				startOnCreate = false;
			} else if (wasStreaming)
				startPreview(cameraId, width, height);
		} catch (CameraException e) {
			logError(e);
		}
	}

	public final void startPreview(int cameraId, int width, int height)
		throws CameraException {
		this.cameraId = cameraId;
		cameraModule.configureCamera(cameraId, width, height);
		cameraModule.startPreview(cameraView);
	}

	public int getCameraId() {
		return cameraId;
	}

	public final void stopPreview() {
		cameraModule.stopPreview();
	}

	public final boolean isPreviewActive() {
		return cameraModule.isStreaming();
	}

	@Override
	protected void onDestroy() {
		cameraModule.disposeCamera();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		logDebug("Surface holder created: " + holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		logDebug("Surface holder destroyed: " + holder);
	}
}

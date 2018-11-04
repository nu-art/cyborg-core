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

import android.Manifest.permission;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceView;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.crashReport.ModuleStateCollector;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@ModuleDescriptor(features = {
	"android.hardware.camera",
	"android.hardware.camera.autofocus"
},
                  usesPermissions = {permission.CAMERA})
public final class CameraModule
	extends CyborgModule
	implements ModuleStateCollector {

	public static final int CAMERA_FACING_BACK = 0;

	public static final int CAMERA_FACING_FRONT = 1;

	private int cameraId = -1;

	private Camera camera;

	private CameraState state;

	private boolean older = false;

	private int cameraCount;
	private MediaRecorder mediaRecorder;
	private CamcorderProfile profile;

	@Override
	protected void init() {
		try {
			cameraCount = Camera.getNumberOfCameras();
		} catch (NoSuchMethodError e) {
			older = true;
			cameraCount = 1;
		}
	}

	private void setState(CameraState state) {
		if (this.state == state)
			return;

		logInfo("On state changed: " + this.state + " => " + state);
		this.state = state;
	}

	@Override
	protected void printModuleDetails() {
		if (older) {
			logInfo("Camera Info #" + 0 + ": Older device... Single Camera... Unknown details...");
			return;
		}

		for (int i = 0; i < cameraCount; i++) {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(i, cameraInfo);
			logInfo("Camera Info #" + i + ": " + cameraInfo + ", facing == " + cameraInfo.facing + ", orientation == " + cameraInfo.orientation);
		}
	}

	private void acquireCamera(int cameraId)
		throws CameraException {
		if (this.cameraId == cameraId && camera != null)
			return;

		try {
			if (older) {
				/*
				 * AOS <= 2.3
				 */
				camera = Camera.open();
				cameraId = 0;
			} else {
				/*
				 * AOS > 2.3
				 */
				camera = Camera.open(cameraId);
				this.cameraId = cameraId;
			}
			setState(CameraState.Acquired);
		} catch (Throwable e) {
			throw new CameraException("Failed to acquire the Camera #" + cameraId, e);
		}
	}

	private void setCameraDisplayOrientation() {
		if (camera == null)
			throw new BadImplementationException("Camera NOT initialized properly!! camera == null");

		int screenOrientationDegrees = getSystemService(WindowService).getDefaultDisplay().getRotation() * 90;

		int facing;
		int cameraOrientationDegrees;
		if (older) {
			facing = 0;
			cameraOrientationDegrees = 90;
		} else {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, cameraInfo);
			facing = cameraInfo.facing;
			cameraOrientationDegrees = cameraInfo.orientation;
		}

		int orientationDegreesOffset;

		if (facing == CAMERA_FACING_FRONT) {
			orientationDegreesOffset = (cameraOrientationDegrees + screenOrientationDegrees) % 360;
			orientationDegreesOffset = (360 - orientationDegreesOffset) % 360; // compensate the mirror
		} else { // back-facing
			orientationDegreesOffset = (cameraOrientationDegrees - screenOrientationDegrees + 360) % 360;
		}
		logInfo("Setting new orientation offset to Camera #" + cameraId + ": " + orientationDegreesOffset + " deg");
		camera.setDisplayOrientation(orientationDegreesOffset);
	}

	/**
	 * The orientation can be received from: <br>
	 * activity.getWindowManager().getDefaultDisplay().getRotation();
	 */
	public final void configureCamera(int cameraId, int width, int height)
		throws CameraException {
		acquireCamera(cameraId);
		setCameraDisplayOrientation();

		// We need to make sure that our preview and recording video size are supported by the
		// camera. Query camera to find all the sizes and choose the optimal size given the
		// dimensions of our preview surface.
		Parameters parameters = camera.getParameters();
		List<Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
		List<Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
		Size optimalSize = getOptimalVideoSize(mSupportedVideoSizes, mSupportedPreviewSizes, width, height);

		// Use the same size for recording profile.
		profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
		profile.videoFrameWidth = optimalSize.width;
		profile.videoFrameHeight = optimalSize.height;

		try {
			logInfo("Setting new preview size to Camera #" + cameraId + ": (" + optimalSize.width + ", " + optimalSize.height + ")");
			parameters.setPreviewSize(optimalSize.width, optimalSize.height);
			camera.setParameters(parameters);
			setState(CameraState.Configured);
		} catch (Throwable t) {
			throw new CameraException("Error configuring Camera: " + parameters, t);
		}
	}

	public final boolean isRecording() {
		return mediaRecorder != null;
	}

	public final void startRecording(String outputFile)
		throws CameraException {
		if (mediaRecorder != null)
			throw new CameraException("Error recording is already in progress");

		if (camera == null)
			throw new CameraException("Error must first enable the camera");

		mediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		camera.unlock();
		mediaRecorder.setCamera(camera);

		// Step 2: Set sources
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mediaRecorder.setProfile(profile);

		// Step 4: Set output file
		mediaRecorder.setOutputFile(outputFile);
		// END_INCLUDE (configure_media_recorder)

		// Step 5: Prepare configured MediaRecorder
		try {
			mediaRecorder.prepare();
		} catch (Exception e) {
			stopRecording();
			throw new CameraException("Error while preparing media recorder", e);
		}
		mediaRecorder.start();
	}

	public final void stopRecording() {
		if (mediaRecorder == null)
			return;

		// clear recorder configuration
		mediaRecorder.reset();
		// release the recorder object
		mediaRecorder.release();
		mediaRecorder = null;
		// Lock camera for later use i.e taking it back from MediaRecorder.
		// MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
		camera.lock();
	}

	public final void startPreview(SurfaceView cameraSurfaceView)
		throws CameraException {
		if (camera == null)
			throw new BadImplementationException("Camera NOT initialized properly!! camera == null");

		if (state == CameraState.Streaming)
			throw new BadImplementationException("Calling to start the camera preview while it is ALREADY active");

		if (state != CameraState.Configured)
			throw new BadImplementationException("Calling to start the camera preview while it is NOT configured");

		try {
			camera.reconnect();
		} catch (IOException e) {
			throw new CameraException("Error reconnecting to camera.", e);
		}

		try {
			camera.setPreviewDisplay(cameraSurfaceView.getHolder());
		} catch (Throwable t) {
			throw new CameraException("Error setting preview display.", t);
		}

		try {
			camera.startPreview();
			setState(CameraState.Streaming);
		} catch (Throwable t) {
			throw new CameraException("Error starting preview display.", t);
		}
	}

	public final void stopPreview() {
		if (state != CameraState.Streaming)
			throw new BadImplementationException("Calling to stop the camera preview while it is NOT active");

		camera.stopPreview();
		setState(CameraState.Configured);
	}

	public final void disposeCamera() {
		if (state == CameraState.Streaming)
			camera.stopPreview();

		if (camera != null)
			camera.release();

		camera = null;
		setState(CameraState.NotAcquired);
	}

	public final boolean isStreaming() {
		return state == CameraState.Streaming;
	}

	public final int getCameraCount() {
		return cameraCount;
	}

	@Override
	public void collectModuleState(HashMap<String, Object> moduleCrashData) {
		moduleCrashData.put("state", state);
	}

	public static Camera.Size getOptimalVideoSize(List<Camera.Size> supportedVideoSizes, List<Camera.Size> previewSizes, int w, int h) {
		// Use a very small tolerance because we want an exact match.
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;

		// Supported video sizes list might be null, it means that we are allowed to use the preview
		// sizes
		List<Camera.Size> videoSizes;
		if (supportedVideoSizes != null) {
			videoSizes = supportedVideoSizes;
		} else {
			videoSizes = previewSizes;
		}
		Camera.Size optimalSize = null;

		// Start with max value and refine as we iterate over available video sizes. This is the
		// minimum difference between view and camera height.
		double minDiff = Double.MAX_VALUE;

		// Target view height
		int targetHeight = h;

		// Try to find a video size that matches aspect ratio and the target view size.
		// Iterate over all available sizes and pick the largest size that can fit in the view and
		// still maintain the aspect ratio.
		for (Camera.Size size : videoSizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find video size that matches the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : videoSizes) {
				if (Math.abs(size.height - targetHeight) < minDiff && previewSizes.contains(size)) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
}

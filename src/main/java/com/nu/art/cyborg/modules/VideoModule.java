///*
// * cyborg-core is an extendable  module based framework for Android.
// *
// * Copyright (C) 2018  Adam van der Kruk aka TacB0sS
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.nu.art.cyborg.modules;
//
//import android.util.Log;
//
//import com.nu.art.cyborg.core.CyborgModule;
//
//public class VideoModule
//	extends CyborgModule {
//
//	@Override
//	protected void init() {
//
//	}
//
//	public void startRecording() {
//		if (isRecording) {
//			stopRecording();
//			return;
//		}
//
//		// BEGIN_INCLUDE(prepare_start_media_recorder)
//
//		new MediaPrepareTask().execute(null, null, null);
//
//		// END_INCLUDE(prepare_start_media_recorder)
//	}
//
//	private void stopRecording() {
//		// BEGIN_INCLUDE(stop_release_media_recorder)
//		// if we are using MediaRecorder, release it first
//		releaseMediaRecorder();
//		// release the camera immediately on pause event
//		releaseCamera();
//
//		// stop recording and release camera
//		try {
//			mMediaRecorder.stop();  // stop the recording
//		} catch (RuntimeException e) {
//			// RuntimeException is thrown when stop() is called immediately after start().
//			// In this case the output file is not properly constructed ans should be deleted.
//			Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
//			//noinspection ResultOfMethodCallIgnored
//			mOutputFile.delete();
//		}
//		releaseMediaRecorder(); // release the MediaRecorder object
//		mCamera.lock();         // take camera access back from MediaRecorder
//
//		// inform the user that recording has stopped
//		setCaptureButtonText("Capture");
//		isRecording = false;
//		releaseCamera();
//	}
//}

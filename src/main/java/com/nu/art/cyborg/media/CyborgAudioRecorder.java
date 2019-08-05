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

package com.nu.art.cyborg.media;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.Condition;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.core.tools.FileTools;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.ThreadsModule;
import com.nu.art.cyborg.modules.PermissionModule;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static android.media.MediaRecorder.OutputFormat.DEFAULT;
import static com.nu.art.cyborg.media.CyborgAudioRecorder.AudioRecorderState.Idle;
import static com.nu.art.cyborg.media.CyborgAudioRecorder.AudioRecorderState.Preparing;
import static com.nu.art.cyborg.media.CyborgAudioRecorder.AudioRecorderState.Recording;

public class CyborgAudioRecorder
	extends CyborgModule {

	public enum AudioChannelType {
		CHANNEL_IN_MONO(AudioFormat.CHANNEL_IN_MONO),
		CHANNEL_IN_STEREO(AudioFormat.CHANNEL_IN_STEREO);
		public final int key;

		AudioChannelType(int key) {this.key = key;}
	}

	public enum SampleRateType {
		_8000(8000),
		_11025(11025),
		_16000(16000),
		_22050(22050),
		_32000(32000),
		_44100(44100),
		_48000(48000),
		//
		;
		public final int key;

		SampleRateType(int key) {this.key = key;}
	}

	public enum EncodingType {
		ENCODING_PCM_16BIT(AudioFormat.ENCODING_PCM_16BIT),
		ENCODING_PCM_8BIT(AudioFormat.ENCODING_PCM_8BIT),
		ENCODING_DEFAULT(AudioFormat.ENCODING_DEFAULT),
		ENCODING_PCM_FLOAT(AudioFormat.ENCODING_PCM_FLOAT),
		ENCODING_AC3(AudioFormat.ENCODING_AC3),
		ENCODING_E_AC3(AudioFormat.ENCODING_E_AC3),
		ENCODING_DTS(AudioFormat.ENCODING_DTS),
		ENCODING_DTS_HD(AudioFormat.ENCODING_DTS_HD),
		ENCODING_IEC61937(AudioFormat.ENCODING_IEC61937),
		ENCODING_DOLBY_TRUEHD(AudioFormat.ENCODING_DOLBY_TRUEHD),
		//
		;
		public final int key;

		EncodingType(int key) {this.key = key;}
	}

	public enum AudioSourceType {
		MIC(MediaRecorder.AudioSource.MIC),
		DEFAULT(MediaRecorder.AudioSource.DEFAULT),
		CAMCORDER(MediaRecorder.AudioSource.CAMCORDER),
		VOICE_RECOGNITION(MediaRecorder.AudioSource.VOICE_RECOGNITION),
		VOICE_COMMUNICATION(MediaRecorder.AudioSource.VOICE_COMMUNICATION),
		REMOTE_SUBMIX(MediaRecorder.AudioSource.REMOTE_SUBMIX),
		UNPROCESSED(MediaRecorder.AudioSource.UNPROCESSED),;

		public final int key;

		AudioSourceType(int key) {this.key = key;}
	}

	public interface AudioBufferProcessor {

		void process(ArrayList<ByteBuffer> buffer, int byteRead, int sampleRate);
	}

	public enum AudioRecorderState {
		Idle,
		Preparing,
		Recording
	}

	@TargetApi(VERSION_CODES.N)
	public enum AudioRecorderError {
		Unknown(0),
		// Technically this should never come back zero... because AudioSystem.SUCCESS == 0... so a precaution?
		Error(AudioRecord.ERROR),
		BadValue(AudioRecord.ERROR_BAD_VALUE),
		DeadObject(AudioRecord.ERROR_DEAD_OBJECT),
		InvalidOperation(AudioRecord.ERROR_INVALID_OPERATION),
		// DO NOT TOUCH THIS COMMENT!!
		;

		private final int value;

		AudioRecorderError(int value) {
			this.value = value;
		}

		private static AudioRecorderError getErrorFromValue(final int bufferSize) {
			return ReflectiveTools.findMatchingEnumItem(AudioRecorderError.class, new Condition<AudioRecorderError>() {
				@Override
				public boolean checkCondition(AudioRecorderError item) {
					return item.value == bufferSize;
				}
			});
		}
	}

	public interface AudioRecorderErrorListener {

		void onError();

		void onErrorReadingBuffer();

		void onErrorInitializingAudioRecord();

		void onErrorGettingBufferSize();

		void onNoPermission();

		void onRecordingToFileError();
	}

	public interface AudioRecorderStateListener {

		void onAudioRecorderStateChanged();
	}

	private static final int SampleRate = 16000;
	private static final int RecordingEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private static final int RecordingChannel = AudioFormat.CHANNEL_IN_MONO;
	private static final int RecordingSource = AudioSource.VOICE_RECOGNITION;
	private static final int MaxBufferSize = 3;

	private AudioRecord audioRecord;
	private RecorderBuilder currentBuilder;
	private Handler recorderHandler;
	private AudioBufferProcessor[] listeners = {};
	private AtomicBoolean record = new AtomicBoolean(false);

	private ArrayList<ByteBuffer> buffer = new ArrayList<ByteBuffer>() {
		@Override
		public void clear() {
			if (isBuffering)
				return;

			super.clear();
		}
	};

	private boolean isBuffering;

	private AtomicReference<AudioRecorderState> state = new AtomicReference<>(Idle);

	@Override
	protected void init() {
		recorderHandler = getModule(ThreadsModule.class).getDefaultHandler("Audio Recorder", android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	}

	@Deprecated
	public final RecorderBuilder createBuilder() {
		return new RecorderBuilder();
	}

	@Deprecated
	public final RecordToFileBuilder createFileRecorderBuilder() {
		return new RecordToFileBuilder();
	}

	public void setState(AudioRecorderState state) {
		AudioRecorderState previousState = this.state.getAndSet(state);
		dispatchGlobalEvent("State: " + previousState + " => " + state, AudioRecorderStateListener.class, new Processor<AudioRecorderStateListener>() {
			@Override
			public void process(AudioRecorderStateListener listener) {
				listener.onAudioRecorderStateChanged();
			}
		});
	}

	public AudioRecorderState getState() {
		return state.get();
	}

	public final boolean isState(AudioRecorderState state) {
		return this.state.get() == state;
	}

	public final boolean isRecording() {
		return isState(Recording);
	}

	public final void addListener(AudioBufferProcessor listener) {
		if (ArrayTools.contains(listeners, listener))
			return;

		listeners = ArrayTools.appendElement(listeners, listener);
	}

	public final void removeListener(AudioBufferProcessor listener) {
		listeners = ArrayTools.removeElement(listeners, listener);
	}

	private void process(ByteBuffer buffer, int byteRead, int sampleRate) {
		this.buffer.add(buffer);
		while (this.buffer.size() > currentBuilder.maxBufferSize)
			this.buffer.remove(0);

		if (DebugFlag.isEnabled())
			logDebug("Recording buffer, buffer size(" + this.buffer.size() + "), listeners size(" + listeners.length + ")");

		for (AudioBufferProcessor listener : listeners) {
			listener.process(this.buffer, byteRead, sampleRate);
		}

		this.buffer.clear();
	}

	public void setBuffering(boolean isBuffering) {
		if (this.isBuffering == isBuffering)
			return;

		logDebug("Buffering: " + isBuffering);
		this.isBuffering = isBuffering;
	}

	private void prepare(RecorderBuilder builder) {
		logInfo("Starting Recorder: " + getRecorderBuilderDetails(builder));

		if (!getModule(PermissionModule.class).isPermissionGranted(permission.RECORD_AUDIO)) {
			dispatchErrorNoPermission();
			return;
		}

		int bufferSize;
		try {
			bufferSize = builder.calculateBufferSize();
			if (DebugFlag.isEnabled())
				logDebug("BufferSize: " + bufferSize);
		} catch (AudioRecordingException e) {
			dispatchErrorGettingBufferSize(e);
			setState(Idle);
			return;
		}

		try {
			audioRecord = builder.createAudioRecord(bufferSize);
		} catch (AudioRecordingException e) {
			dispatchErrorInitializingAudioRecord(e);
			setState(Idle);
			return;
		}

		currentBuilder = builder;
		recordImpl(bufferSize, builder.sampleRate);
	}

	@NonNull
	private String getRecorderBuilderDetails(RecorderBuilder builder) {
		return "\n  recordingSource: " + builder.recordingSource + //
			"\n  recordingChannel: " + builder.recordingChannel + //
			"\n  recordingEncoding: " + builder.recordingEncoding +//
			"\n  sampleRate: " + builder.sampleRate;
	}

	private void recordImpl(int bufferSize, int sampleRate) {
		audioRecord.startRecording();
		setState(Recording);

		while (record.get()) {
			int byteRead;
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize * 2);
			byteRead = audioRecord.read(buffer, bufferSize * 2);

			if (record.get() && byteRead <= 0) {
				dispatchErrorReadingBuffer();
				break;
			}

			process(buffer, byteRead, sampleRate);
		}

		try {
			audioRecord.stop();
		} catch (IllegalStateException e) {
			logWarning("Error stopping... is this message really needed ???????", e);
		}

		try {
			audioRecord.release();
		} catch (Exception e) {
			logWarning("Error releasing... is this message really needed ???????", e);
		}

		audioRecord = null;
		setState(Idle);
	}

	private void dispatchErrorReadingBuffer() {
		dispatchModuleEvent("Error reading buffer", AudioRecorderErrorListener.class, new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {
				listener.onErrorReadingBuffer();
			}
		});
	}

	private void dispatchErrorInitializingAudioRecord(AudioRecordingException e) {
		dispatchModuleEvent("Error audio record will not initialize", AudioRecorderErrorListener.class, new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {
				listener.onErrorInitializingAudioRecord();
			}
		});
	}

	private void dispatchErrorGettingBufferSize(AudioRecordingException e) {
		dispatchModuleEvent("Error getting audio buffer size", AudioRecorderErrorListener.class, new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {
				listener.onErrorGettingBufferSize();
			}
		});
	}

	private void dispatchErrorNoPermission() {
		dispatchModuleEvent("Error Starting Audio Recorder - No Permission", AudioRecorderErrorListener.class, new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {
				listener.onNoPermission();
			}
		});
	}

	@SuppressWarnings("unused")
	public class RecorderBuilder
		extends BaseRecorderBuilder {

		private RecorderBuilder() {}

		public final void startRecording() {
			if (record.get()) {
				logDebug("Already recording...");
				return;
			}

			record.set(true);
			setState(Preparing);
			recorderHandler.post(this);
		}

		int calculateBufferSize()
			throws AudioRecordingException {
			int bufferSize = AudioRecord.getMinBufferSize(sampleRate, recordingChannel, recordingEncoding);
			if (bufferSize <= 0)
				checkForBufferError(bufferSize);

			// this 1024 might need to be a calculated value according to the builder configuration... not sure AT ALL.. but just a thought... need ot ask an expert about it!
			while (bufferSize < 4096)
				bufferSize *= 2;

			return bufferSize;
		}

		@Override
		public void run() {
			if (!record.get()) {
				logDebug("Recorder was already stopped before it even begun... aborting");
				return;
			}

			prepare(this);
		}

		private AudioRecord createAudioRecord(int bufferSize)
			throws AudioRecordingException {
			AudioRecord audioRecord = new AudioRecord(recordingSource, sampleRate, recordingChannel, recordingEncoding, bufferSize);
			if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED)
				return audioRecord;

			throw new AudioRecordingException("Audio Record will not initialized");
		}

		private void checkForBufferError(int bufferSize)
			throws AudioRecordingException {
			AudioRecorderError error = AudioRecorderError.getErrorFromValue(bufferSize);

			if (error != null)
				throw new AudioRecordingException("Error getting min buffer size: " +//
					                                  "\n  error: " + error.name());
		}
	}

	public final void stopRecording() {
		recorderHandler.removeCallbacksAndMessages(null);
		record.set(false);

		if (audioRecord == null)
			return;

		logInfo("Called Stop Recording...");
	}

	public class AudioRecordingException
		extends Exception {

		AudioRecordingException(String message) {
			super(message);
		}
	}

	public abstract class BaseRecorderBuilder<T extends BaseRecorderBuilder>
		implements Runnable {

		int sampleRate = SampleRate;
		int recordingEncoding = RecordingEncoding;
		int recordingChannel = RecordingChannel;
		int recordingSource = RecordingSource;
		int maxBufferSize = MaxBufferSize;

		public abstract void startRecording();

		public T setRecordingEncoding(int recordingEncoding) {
			this.recordingEncoding = recordingEncoding;
			return (T) this;
		}

		public T setRecordingSource(int recordingSource) {
			this.recordingSource = recordingSource;
			return (T) this;
		}

		public T setMaxBufferSize(int maxBufferSize) {
			this.maxBufferSize = maxBufferSize;
			return (T) this;
		}

		public T setSampleRate(int sampleRate) {
			this.sampleRate = sampleRate;
			return (T) this;
		}

		public T setRecordingChannel(int recordingChannel) {
			this.recordingChannel = recordingChannel;
			return (T) this;
		}

		@Override
		public void run() {}
	}

	public class RecordToFileBuilder
		extends BaseRecorderBuilder {

		private RecorderProgressListener listener;
		private MediaRecorder recorder;
		private int outputFormat = DEFAULT;
		private String outputFile;
		private long recordingStarted;
		private int progressDelay = 999;
		Runnable progressUpdater = new Runnable() {
			@Override
			public void run() {
				if (listener != null)
					listener.onProgress(System.currentTimeMillis() - recordingStarted);

				if (!record.get())
					return;

				postOnUI(progressDelay, this);
			}
		};

		public RecordToFileBuilder() {}

		public RecordToFileBuilder setOutputfile(String outputFile) {
			this.outputFile = outputFile;
			return this;
		}

		public RecordToFileBuilder setOutputFormat(int outputFormat) {
			this.outputFormat = outputFormat;
			return this;
		}

		public RecordToFileBuilder setProgressListener(RecorderProgressListener listener) {
			this.listener = listener;
			return this;
		}

		public RecordToFileBuilder setProgressDelay(int progressDelay) {
			this.progressDelay = progressDelay;
			return this;
		}

		@Override
		public void startRecording() {
			if (!getModule(PermissionModule.class).isPermissionGranted(permission.RECORD_AUDIO)) {
				dispatchErrorNoPermission();
				return;
			}
			if (record.get()) {
				logDebug("Already recording...");
				return;
			}

			record.set(true);
			setState(Preparing);
			recorderHandler.post(this);
		}

		public void stopRecording() {
			recorderHandler.removeCallbacksAndMessages(null);
			removeActionFromBackground(progressUpdater);
			setState(Idle);
			if (recorder == null)
				return;

			recorder.stop();
			recorder.release();
			recorder = null;

			record.set(false);

			logInfo("Called Stop Recording To File...");
		}

		@Override
		public void run() {
			if (recorder != null)
				stopRecording();

			recorder = new MediaRecorder();
			recorder.setAudioSource(recordingSource);
			recorder.setOutputFormat(outputFormat);
			recorder.setAudioEncoder(recordingEncoding);
			recorder.setOutputFile(outputFile);

			recorder.setOnInfoListener(new OnInfoListener() {
				@Override
				public void onInfo(MediaRecorder mr, int what, int extra) {
					stopRecording(); // needed?
				}
			});

			recorder.setOnErrorListener(new OnErrorListener() {
				@Override
				public void onError(MediaRecorder mr, int what, int extra) {
					stopRecording();
					dispatchRecordingError();
				}
			});

			try {
				final File parentFile = new File(outputFile).getParentFile();
				if (parentFile != null)
					FileTools.mkDir(parentFile);
				recorder.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setState(Recording);
			recorder.start();
			recordingStarted = System.currentTimeMillis();
			postOnUI(990, progressUpdater);
		}

		private void dispatchRecordingError() {
			dispatchEvent("Error Recording to File", AudioRecorderErrorListener.class, new Processor<AudioRecorderErrorListener>() {
				@Override
				public void process(AudioRecorderErrorListener listener) {
					listener.onRecordingToFileError();
				}
			});
		}
	}

	public interface RecorderProgressListener {

		void onProgress(long progress);
	}
}
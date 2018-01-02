package com.nu.art.cyborg.media;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.interfaces.Condition;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.consts.DebugFlags;
import com.nu.art.cyborg.core.modules.ThreadsModule;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.nu.art.cyborg.media.CyborgAudioRecorder.AudioRecorderState.Idle;
import static com.nu.art.cyborg.media.CyborgAudioRecorder.AudioRecorderState.Preparing;
import static com.nu.art.cyborg.media.CyborgAudioRecorder.AudioRecorderState.Recording;

public class CyborgAudioRecorder
		extends CyborgModule {

	public static final String DebugFlag = "Debug_CyborgAudioRecorder";

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
	private final Object sync = new Object();

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

	public final RecorderBuilder createBuilder() {
		return new RecorderBuilder();
	}

	public void setState(AudioRecorderState state) {
		AudioRecorderState previousState = this.state.getAndSet(state);
		logInfo("State: " + previousState + " => " + state);
	}

	public final boolean isState(AudioRecorderState state) {
		return this.state.get() == state;
	}

	public final void addListener(AudioBufferProcessor listener) {
		if (ArrayTools.contains(listeners, listener))
			return;

		listeners = ArrayTools.appendElement(listeners, listener);
	}

	public final void removeListener(AudioBufferProcessor listener) {
		listeners = ArrayTools.removeElement(listeners, listener);
	}

	public final boolean isRecording() {
		return isState(Recording);
	}

	public final void stopRecording() {
		synchronized (sync) {
			if (audioRecord == null)
				return;

			logInfo("Called Stop Recording...");
			audioRecord.stop();
		}
	}

	private void process(ByteBuffer buffer, int byteRead, int sampleRate) {
		this.buffer.add(buffer);
		while (this.buffer.size() > currentBuilder.maxBufferSize)
			this.buffer.remove(0);

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
		logInfo("Starting Recorder: " + //
				getRecorderBuilderDetails(builder));

		if (audioRecord != null) {
			dispatchErrorAlreadyRecording();
			return;
		}

		int bufferSize;
		try {
			bufferSize = builder.calculateBufferSize();
			if (DebugFlags.isDebuggableFlag(DebugFlag))
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

		while (true) {
			int byteRead;
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize * 2);
			byteRead = audioRecord.read(buffer, bufferSize * 2);

			if (byteRead <= 0) {
				dispatchErrorReadingBuffer();
				break;
			}

			process(buffer, byteRead, sampleRate);
		}

		synchronized (sync) {
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
	}

	private void dispatchErrorReadingBuffer() {
		dispatchModuleEvent("Error reading buffer", new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {

			}
		});
	}

	private void dispatchErrorInitializingAudioRecord(AudioRecordingException e) {
		dispatchModuleEvent("Error audio record will not initialize", new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {

			}
		});
	}

	private void dispatchErrorGettingBufferSize(AudioRecordingException e) {
		dispatchModuleEvent("Error getting audio buffer size", new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {

			}
		});
	}

	private void dispatchErrorAlreadyRecording() {
		dispatchModuleEvent("Error Starting Audio Recorder - Already recording", new Processor<AudioRecorderErrorListener>() {
			@Override
			public void process(AudioRecorderErrorListener listener) {

			}
		});
	}

	@SuppressWarnings("unused")
	public class RecorderBuilder
			implements Runnable {

		int maxBufferSize = MaxBufferSize;
		int sampleRate = SampleRate;
		int recordingChannel = RecordingChannel;
		int recordingEncoding = RecordingEncoding;
		int recordingSource = RecordingSource;

		private RecorderBuilder() {
		}

		public RecorderBuilder setRecordingEncoding(int recordingEncoding) {
			this.recordingEncoding = recordingEncoding;
			return this;
		}

		public RecorderBuilder setRecordingSource(int recordingSource) {
			this.recordingSource = recordingSource;
			return this;
		}

		public RecorderBuilder setMaxBufferSize(int maxBufferSize) {
			this.maxBufferSize = maxBufferSize;
			return this;
		}

		public RecorderBuilder setSampleRate(int sampleRate) {
			this.sampleRate = sampleRate;
			return this;
		}

		public RecorderBuilder setRecordingChannel(int recordingChannel) {
			this.recordingChannel = recordingChannel;
			return this;
		}

		public final void startRecording() {
			recorderHandler.post(this);
		}

		int calculateBufferSize()
				throws AudioRecordingException {
			int bufferSize = AudioRecord.getMinBufferSize(sampleRate, recordingChannel, recordingEncoding);
			if (bufferSize <= 0)
				checkForBufferError(bufferSize);

			// this 1024 might need to be a calculated value according to the builder configuration... not sure AT ALL.. but just a thought... need ot ask an expert about it!
			while (bufferSize < 1024)
				bufferSize *= 2;

			return bufferSize;
		}

		@Override
		public void run() {
			setState(Preparing);
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

	public class AudioRecordingException
			extends Exception {

		AudioRecordingException(String message) {
			super(message);
		}
	}
}
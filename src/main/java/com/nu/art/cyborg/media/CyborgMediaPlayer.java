/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
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

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.view.Surface;
import android.view.animation.Interpolator;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.tools.DateTimeTools;
import com.nu.art.cyborg.common.utils.Interpolators;
import com.nu.art.cyborg.core.CyborgModuleItem;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

@SuppressWarnings("WeakerAccess")
public class CyborgMediaPlayer
		extends CyborgModuleItem {

	public final static int MAX_VOLUME = 100;
	private static final int ERROR_TIMED_OUT = 100;

	private final Thread ownerThread = Thread.currentThread();
	private String key;
	private MediaBuilder builder;
	private MediaPlayer mediaPlayer;
	private TimeoutError timeoutError;
	private PlayerState state = PlayerState.Idle;

	private int maxVolume = MAX_VOLUME;
	private int volume = MAX_VOLUME;
	private FadeVolumeRunnable fadingVolume;

	private final InternalListener _listener = new InternalListener();
	private MediaPlayerListener listener;

	private int duration;
	private WeakReference<Surface> surfaceView = new WeakReference<>(null);

	public MediaBuilder createBuilder() {
		return new MediaBuilder();
	}

	public boolean isTag(String key) {
		return builder.tag.equals(key);
	}

	public enum PlayerState {
		Idle,
		Preparing,
		Prepared,
		Playing,
		Disposing,
	}

	public final String getKey() {
		return key;
	}

	final CyborgMediaPlayer setKey(String key) {
		this.key = key;
		setTag(getClass().getSimpleName() + "-" + key);
		return this;
	}

	public void setSurface(Surface surface) {
		this.surfaceView = new WeakReference<>(surface);
		mediaPlayer.setSurface(surface);
	}

	@Override
	protected void init() {
		createMediaPlayer();
	}

	public void setMaxVolume(int maxVolume) {
		logInfo("Max volume is now: " + maxVolume);
		this.maxVolume = maxVolume;
	}

	public float getVolumeRelative() {
		return 1f * volume / maxVolume;
	}

	public final int getMaxVolume() {
		return maxVolume;
	}

	public int getVolume() {
		return this.volume;
	}

	/**
	 * @param soundVolume The volume to set this media player to.
	 */
	public final void setVolume(int soundVolume) {
		int normalizeVolume = normalizeVolume(soundVolume);
		float volume = 1 - (float) ((Math.log(MAX_VOLUME - normalizeVolume) / Math.log(MAX_VOLUME)));
		mediaPlayer.setVolume(volume, volume);
		this.volume = normalizeVolume;
	}

	private int normalizeVolume(int soundVolume) {
		if (soundVolume > maxVolume)
			soundVolume = maxVolume;

		if (soundVolume < 0)
			soundVolume = 0;
		return soundVolume;
	}

	/**
	 * Will cause gradual fade to the volume and will set the max volume of this player while fading
	 *
	 * @param targetVolume The final target fade volume
	 * @param duration     The duration for the fade
	 */
	public void fadeVolumeAndSetItAsMax(final int targetVolume, int duration) {
		fadeVolumeAndSetItAsMax(targetVolume, duration, Interpolators.LinearInterpolator);
	}

	/**
	 * Will cause gradual fade to the volume and will set the max volume of this player while fading
	 *
	 * @param targetVolume The final target fade volume
	 * @param duration     The duration for the fade
	 * @param interpolator The interpolator to apply on fade progress
	 */
	public void fadeVolumeAndSetItAsMax(final int targetVolume, int duration, Interpolator interpolator) {
		logDebug("volume fade to: " + targetVolume + ", max to set: " + maxVolume);

		VolumeFadeListener listener = null;
		if (targetVolume > maxVolume)
			setMaxVolume(targetVolume);
		else
			listener = new VolumeFadeListener() {
				@Override
				public void onFadeCompleted() {
					setMaxVolume(targetVolume);
				}
			};

		fadeVolume(targetVolume, duration, interpolator, listener);
	}

	private void fadeVolume(int targetVolume, final int duration, VolumeFadeListener listener) {
		fadeVolume(targetVolume, duration, Interpolators.LinearInterpolator, listener);
	}

	private void fadeVolume(int targetVolume, final int duration, Interpolator interpolator, VolumeFadeListener listener) {
		targetVolume = normalizeVolume(targetVolume);

		final int fromVolume = this.volume;

		logInfo("fadeVolume: " + fromVolume + " => " + targetVolume + (fadingVolume != null ? "(Already in progress)" : ""));
		if (fadingVolume != null)
			fadingVolume.stop();

		postOnUI(20, fadingVolume = new FadeVolumeRunnable(duration, fromVolume, targetVolume - fromVolume, interpolator, listener));
	}

	public synchronized PlayerState getState() {
		return state;
	}

	private synchronized void setState(PlayerState state) {
		logDebug("Player State: " + this.state + " ==> " + state);
		this.state = state;
	}

	public synchronized boolean isState(PlayerState state) {
		return this.state == state;
	}

	public synchronized boolean isAlive() {
		return this.state == PlayerState.Prepared || state == PlayerState.Playing;
	}

	public synchronized void setPosition(float positionRelative) {
		assertThread();
		if (mediaPlayer == null || builder == null || isAlive())
			return;

		builder.positionMs = (int) (getDuration() * positionRelative);
		mediaPlayer.seekTo(builder.positionMs);
	}

	public synchronized void setPosition(int positionMs) {
		assertThread();

		if (mediaPlayer == null || builder == null)
			return;

		builder.positionMs = positionMs;
		mediaPlayer.seekTo(positionMs);
	}

	public float getPositionRelative() {
		if (getDuration() == 0)
			return 0;
		return 1f * getPosition() / getDuration();
	}

	public int getPosition() {
		if (!isState(PlayerState.Playing) && !isState(PlayerState.Prepared))
			return 0;

		int currentPosition = mediaPlayer.getCurrentPosition();
		if (currentPosition > 15 * DateTimeTools.Hour || currentPosition < 0) // LG BUG
			return 0;

		return currentPosition;
	}

	public synchronized int getDuration() {
		return duration;
	}

	private void startProgressNotifier() {
		final Runnable checkPosition = new Runnable() {

			@Override
			public void run() {
				if (builder == null)
					return;

				if (!isPlaying())
					return;

				int currentPosition = mediaPlayer.getCurrentPosition();
				builder.positionMs = currentPosition;
				_listener.dispatchOnProgress(currentPosition);
				postOnUI(100, this);
			}
		};

		postOnUI(50, checkPosition);
	}

	private void createMediaPlayer() {
		logDebug("Creating new MediaPlayer");
		mediaPlayer = new MediaPlayer();
		setState(PlayerState.Idle);

		Surface surface = surfaceView.get();
		if (surface != null)
			mediaPlayer.setSurface(surface);
		mediaPlayer.setOnErrorListener(_listener);
		mediaPlayer.setOnBufferingUpdateListener(_listener);
		mediaPlayer.setOnCompletionListener(_listener);
		mediaPlayer.setOnPreparedListener(_listener);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		setVolume(volume);
	}

	/**
	 * This will play the media.
	 * keep in mind that you might want to request for the audio focus before playing..
	 */
	public synchronized void play() {
		assertThread();
		setState(PlayerState.Playing);

		mediaPlayer.seekTo(builder.positionMs);
		mediaPlayer.start();
		startProgressNotifier();
		_listener.dispatchOnStarted();
	}

	/**
	 * Will pause the media
	 * keep in mind that you might want to release for the audio focus after pausing..
	 */
	public synchronized void pause() {
		assertThread();

		if (!isState(PlayerState.Playing)) {
			logInfo("NOT playing... will not pause");
			return;
		}

		logInfo("pausing");
		mediaPlayer.pause();
		setState(PlayerState.Prepared);
		_listener.dispatchOnPaused();
	}

	/**
	 * This will dispose of the media player and prepare to play again
	 */
	public synchronized void dispose() {
		assertThread();

		boolean wasPlaying = isState(PlayerState.Playing);
		removeTimeoutTrigger();

		pause();

		logInfo("disposing");
		mediaPlayer.release();
		duration = 0;

		if (wasPlaying)
			_listener.dispatchOnInterrupted();

		listener = null;
		createMediaPlayer();
	}

	private void assertThread() {
		if (Thread.currentThread() != ownerThread)
			throw new BadImplementationException("Must be called on thread: " + ownerThread.getName() + "!!");
	}

	@Override
	public String toString() {
		return key;
	}

	public boolean isPaused() {
		return isState(PlayerState.Prepared);
	}

	public boolean isPlaying() {
		return isState(PlayerState.Playing);
	}

	public boolean isLoading() {
		return isState(PlayerState.Preparing);
	}

	public MediaPlayerListener getListener() {
		return listener;
	}

	public void setListener(MediaPlayerListener listener) {
		this.listener = listener;
	}

	public static abstract class MediaPlayerListenerImpl
			implements MediaPlayerListener {

		public void onPrepared() {}

		public void onPlaying() {}

		public void onMediaProgress(int position) {}

		public void onPaused() {}

		public void onMediaCompleted() {}

		public void onInterrupted() {}

		public void onError() {}
	}

	public interface MediaPlayerListener {

		void onPrepared();

		void onPlaying();

		void onMediaProgress(int position);

		void onPaused();

		void onMediaCompleted();

		void onInterrupted();

		void onError();
	}

	public class MediaBuilder {

		private Object tag;

		private Uri url;

		private int positionMs;

		private MediaPlayerListener listener;

		private int timeout = -1;

		private boolean isLooping;

		private HashMap<String, String> headers;

		private boolean autoPlay;

		public MediaBuilder setLooping(boolean looping) {
			isLooping = looping;
			return this;
		}

		public MediaBuilder setTag(Object tag) {
			this.tag = tag;
			return this;
		}

		public MediaBuilder setAutoPlay(boolean autoPlay) {
			this.autoPlay = autoPlay;
			return this;
		}

		public MediaBuilder setUri(String url) {
			setUri(Uri.parse(url));
			return this;
		}

		public MediaBuilder addHeader(String key, String value) {
			if (headers == null)
				headers = new HashMap<>();
			headers.put("Connection", "keep-alive");
			return this;
		}

		public MediaBuilder setUri(Uri uri) {
			this.url = uri;
			if (tag == null)
				tag = url;
			return this;
		}

		public MediaBuilder setPositionMs(int positionMs) {
			this.positionMs = positionMs;
			return this;
		}

		public MediaBuilder setListener(MediaPlayerListener listener) {
			this.listener = listener;
			return this;
		}

		public MediaBuilder setTimeoutMs(int timeout) {
			this.timeout = timeout;
			return this;
		}

		private void scheduleTimeout(int timeout) {
			if (timeout == -1)
				return;

			scheduleMediaTimeout(timeout);
		}

		public void prepare() {
			assertThread();

			logInfo("Preparing: " + url + " | Seek Position: " + positionMs);
			CyborgMediaPlayer.this.builder = this;
			CyborgMediaPlayer.this.listener = listener;
			removeTimeoutTrigger();
			try {
				mediaPlayer.setLooping(isLooping);
				if (headers == null)
					mediaPlayer.setDataSource(getApplicationContext(), url);
				else
					mediaPlayer.setDataSource(getApplicationContext(), url, headers);

				scheduleTimeout(timeout);
				setState(PlayerState.Preparing);
				mediaPlayer.prepareAsync();
			} catch (Exception e) {
				logError("Error while preparing the media player", e);
				listener.onError();
			}
		}

		public MediaBuilder setUri(@IdRes int resId) {
			url = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + resId);
			return this;
		}
	}

	private void scheduleMediaTimeout(int timeout) {
		removeTimeoutTrigger();
		postOnUI(timeout, timeoutError = new TimeoutError(builder != null ? builder.tag : "No Tag"));

		logDebug("Scheduling media timeout(" + timeout + ") for: " + timeoutError.tag);
	}

	private void removeTimeoutTrigger() {
		removeActionFromUI(timeoutError);
	}

	private class InternalListener
			implements OnPreparedListener, OnCompletionListener, OnErrorListener, OnBufferingUpdateListener {

		@Override
		public void onPrepared(MediaPlayer mp) {
			if (builder != null && new File(builder.url.toString()).exists())
				removeTimeoutTrigger();

			setState(PlayerState.Prepared);
			duration = mediaPlayer.getDuration();

			if (listener != null)
				listener.onPrepared();

			if (builder.autoPlay)
				play();
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			logInfo("onCompletion");
			MediaPlayerListener tempListener = listener;
			pause();
			setPosition(0);

			if (tempListener != null)
				tempListener.onMediaCompleted();
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			logError("onError what:" + what + " extra: " + extra);
			MediaPlayerListener tempListener = listener;

			dispose();
			if (tempListener != null)
				tempListener.onError();

			// TBD: not really sure what to return here
			return true;
		}

		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			removeTimeoutTrigger();
		}

		private void dispatchOnStarted() {
			if (listener != null)
				listener.onPlaying();
		}

		private void dispatchOnPaused() {
			if (listener != null)
				listener.onPaused();
		}

		private void dispatchOnProgress(int currentPosition) {
			if (listener != null)
				listener.onMediaProgress(currentPosition);
		}

		private void dispatchOnInterrupted() {
			if (listener != null)
				listener.onInterrupted();
		}
	}

	public interface VolumeFadeListener {

		void onFadeCompleted();
	}

	private class FadeVolumeRunnable
			implements Runnable {

		private final long startTime;
		private final int duration;

		private final int fromVolume;
		private final int deltaVolume;

		private final VolumeFadeListener listener;
		private final Interpolator interpolator;

		private boolean stop;

		public FadeVolumeRunnable(int duration, int fromVolume, int deltaVolume, Interpolator interpolator, VolumeFadeListener listener) {
			startTime = System.currentTimeMillis();
			this.duration = duration;

			this.fromVolume = fromVolume;
			this.deltaVolume = deltaVolume;

			this.listener = listener;
			this.interpolator = interpolator;
		}

		void stop() {
			stop = true;
		}

		@Override
		public void run() {
			if (stop) {
				fadingVolume = null;
				return;
			}

			float intervalPassed = (System.currentTimeMillis() - startTime);
			if (intervalPassed >= duration)
				intervalPassed = duration;

			setVolume(fromVolume + (int) (interpolator.getInterpolation(intervalPassed / duration) * deltaVolume));

			if (intervalPassed != duration) {
				postOnUI(20, this);
				return;
			}

			if (listener != null)
				listener.onFadeCompleted();

			fadingVolume = null;
		}
	}

	private class TimeoutError
			implements Runnable {

		Object tag;

		TimeoutError(Object tag) {
			this.tag = tag;
		}

		@Override
		public void run() {
			logError("Preparing timeout triggered: " + timeoutError.tag);

			_listener.onError(mediaPlayer, ERROR_TIMED_OUT, 0);
		}
	}
}
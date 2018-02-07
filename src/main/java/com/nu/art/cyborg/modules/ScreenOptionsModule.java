package com.nu.art.cyborg.modules;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.provider.Settings.System;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.nu.art.cyborg.core.CyborgModule;

import java.lang.ref.WeakReference;

/**
 * Created by matankoby on 1/30/18.
 */
public class ScreenOptionsModule
		extends CyborgModule {

	public static final int UNKNOWN = -1;
	public static final int LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	public static final int PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	private WeakReference<Activity> weakRefActivity;

	@Override
	protected void init() {

	}

	public final void setActivity(Activity activity) {
		if (activity == null) {
			weakRefActivity = null;
			return;
		}
		weakRefActivity = new WeakReference<Activity>(activity);
	}

	private Window getWindow() {
		if (weakRefActivity == null || weakRefActivity.get() == null)
			return null;

		return weakRefActivity.get().getWindow();
	}

	private Activity getActivity() {
		return weakRefActivity.get();
	}

	/**
	 * brightnessLevel should be between 0 and 255
	 */
	public void setWindowBrightness(int brightnessLevel) {
		if (getWindow() == null) {
			logWarning("Will not set brightness... no window");
			return;
		}
		Window window = getWindow();
		if (window == null) {
			logWarning("Will not set brightness... no window");
			return;
		}

		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = getBrightnessAsFloat(brightnessLevel);
		logDebug("Setting WINDOW Screen Brightness: " + lp.screenBrightness);
		window.setAttributes(lp);
	}

	private float getBrightnessAsFloat(int brightnessLevel) {
		float value = normalizeBrightnessLevel(brightnessLevel) / 255f;
		if (value == 0)
			value = 0.0001f;

		return value;
	}

	private int normalizeBrightnessLevel(int brightnessLevel) {
		if (brightnessLevel > 255)
			brightnessLevel = 255;

		if (brightnessLevel < 0)
			brightnessLevel = 0;

		return brightnessLevel;
	}

	public int getWindowBrightness() {
		if (getWindow() == null) {
			logWarning("Cannot get brightness... no window");
			return UNKNOWN;
		}
		Window window = getWindow();
		if (window == null) {
			logWarning("Cannot get brightness... no window");
			return UNKNOWN;
		}

		WindowManager.LayoutParams lp = window.getAttributes();
		return lp.screenBrightness == UNKNOWN ? UNKNOWN : (int) ((255f) * lp.screenBrightness);
	}

	/**
	 * Requires system permissions
	 */
	public int getSystemScreenBrightnessLevel() {
		return System.getInt(getApplicationContext().getContentResolver(), System.SCREEN_BRIGHTNESS, 0);
	}

	/**
	 * Requires system permissions
	 * brightnessLevel should be between 0 and 255
	 */
	public void setSystemScreenBrightnessLevel(int brightnessLevel) {
		brightnessLevel = normalizeBrightnessLevel(brightnessLevel);
		logDebug("Setting SYSTEM Screen Brightness: " + brightnessLevel);
		Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessLevel);
	}

	/**
	 * One of {@link ScreenOptionsModule#LANDSCAPE}, {@link ScreenOptionsModule#PORTRAIT}.
	 *
	 * @param orientation
	 */
	public void setScreenOrientation(int orientation) {
		if (getActivity() == null) {
			logError("Will not change orientation... no activity");
			return;
		}

		getActivity().setRequestedOrientation(orientation);
	}

	public int getScreenOrientation() {
		Display screenOrientation = getSystemService(WindowService).getDefaultDisplay();

		if (screenOrientation.getWidth() < screenOrientation.getHeight())
			return PORTRAIT;

		return LANDSCAPE;
	}
}
package com.nu.art.cyborg.modules;

import android.app.Activity;
import android.provider.Settings;
import android.provider.Settings.System;
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
	WeakReference<Window> window;

	@Override
	protected void init() {

	}

	public final void setActivity(Activity activity) {
		if (activity == null) {
			window = null;
			return;
		}
		window = new WeakReference<Window>(activity.getWindow());
	}

	/**
	 * brightnessLevel should be between 0 and 255
	 */
	public void setWindowBrightness(int brightnessLevel) {
		if (this.window == null) {
			logWarning("Will not set brightness... no window");
			return;
		}
		Window window = this.window.get();
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
		return normalizeBrightnessLevel(brightnessLevel) / 255f;
	}

	private int normalizeBrightnessLevel(int brightnessLevel) {
		if (brightnessLevel > 255)
			brightnessLevel = 255;

		if (brightnessLevel < 0)
			brightnessLevel = 0;

		return brightnessLevel;
	}

	public int getWindowBrightness() {
		if (this.window == null) {
			logWarning("Cannot get brightness... no window");
			return UNKNOWN;
		}
		Window window = this.window.get();
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
}
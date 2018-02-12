package com.nu.art.cyborg.modules;

import android.app.Activity;
import android.provider.Settings;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager;

import com.nu.art.core.interfaces.Condition;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.reflection.tools.ReflectiveTools;

import java.lang.ref.WeakReference;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;

/**
 * Created by matankoby on 1/30/18.
 */
public class ScreenOptionsModule
		extends CyborgModule {

	public static final int UNKNOWN = -1;
	private WeakReference<Activity> weakRefActivity = new WeakReference<>(null);

	@Override
	protected void init() {}

	public final void setActivity(Activity activity) {
		if (activity == null) {
			weakRefActivity = null;
			return;
		}
		weakRefActivity = new WeakReference<>(activity);
	}

	private Window getWindow() {
		if (weakRefActivity.get() == null)
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
	 * @param orientation One of {@link ScreenOrientation}.
	 */
	public void setRequestScreenOrientation(ScreenOrientation orientation) {
		Activity activity = getActivity();
		if (activity == null) {
			logError("Will not change orientation... no activity");
			return;
		}

		logInfo("Change Orientation State: " + getRequestedScreenOrientation() + " => " + orientation);
		activity.setRequestedOrientation(orientation.value);
	}

	/**
	 * @return Requested orientation.
	 */
	public ScreenOrientation getRequestedScreenOrientation() {
		return ScreenOrientation.getOrientationByValue(getActivity().getRequestedOrientation());
	}

	/**
	 * @return 'Are we landscape or portrait?', for cases where getRequestedScreenOrientation returns e.g "FULL_USER" which doesn't tell you if the device is
	 * portrait/landscape.
	 */
	public boolean isLandscape() {
		return getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels;
	}

	public enum ScreenOrientation {
		UNSPECIFIED(SCREEN_ORIENTATION_UNSPECIFIED),
		LANDSCAPE(SCREEN_ORIENTATION_LANDSCAPE),
		PORTRAIT(SCREEN_ORIENTATION_PORTRAIT),
		USER(SCREEN_ORIENTATION_USER),
		BEHIND(SCREEN_ORIENTATION_BEHIND),
		SENSOR(SCREEN_ORIENTATION_SENSOR),
		NOSENSOR(SCREEN_ORIENTATION_NOSENSOR),
		SENSOR_LANDSCAPE(SCREEN_ORIENTATION_SENSOR_LANDSCAPE),
		SENSOR_OIRTRAUT(SCREEN_ORIENTATION_SENSOR_PORTRAIT),
		REVERSE_LANDSCAPE(SCREEN_ORIENTATION_REVERSE_LANDSCAPE),
		REVERSE_PORTRAIT(SCREEN_ORIENTATION_REVERSE_PORTRAIT),
		FULL_SENSOR(SCREEN_ORIENTATION_FULL_SENSOR),
		USER_LANDSCAPE(SCREEN_ORIENTATION_USER_LANDSCAPE),
		USER_PORTRAIT(SCREEN_ORIENTATION_USER_PORTRAIT),
		FULL_USER(SCREEN_ORIENTATION_FULL_USER),
		LOCKED(SCREEN_ORIENTATION_LOCKED);

		public final int value;

		ScreenOrientation(int value) {
			this.value = value;
		}

		public static ScreenOrientation getOrientationByValue(final int value) {
			return ReflectiveTools.findMatchingEnumItem(ScreenOrientation.class, new Condition<ScreenOrientation>() {
				@Override
				public boolean checkCondition(ScreenOrientation item) {
					return item.value == value;
				}
			});
		}

	}
}
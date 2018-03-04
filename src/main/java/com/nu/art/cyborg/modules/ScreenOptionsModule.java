package com.nu.art.cyborg.modules;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.provider.Settings.System;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

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

	private static final int UNKNOWN = -1;
	private WeakReference<Activity> weakRefActivity = new WeakReference<>(null);
	private Boolean stateKeepScreenAwake = null;
	private Boolean stateFullScreen = null;
	private int brightnessLevel;

	@Override
	protected void init() {}

	public final void setActivity(Activity activity) {
		weakRefActivity = new WeakReference<>(activity);
		if (activity == null)
			return;

		setWindowBrightnessImpl();
		keepScreenAwakeImpl();
		setFullScreenImpl();
	}

	private Window getWindow() {
		Activity activity = getActivity();
		if (activity == null)
			return null;

		return activity.getWindow();
	}

	private Activity getActivity() {
		return weakRefActivity.get();
	}

	/**
	 * brightnessLevel should be between 0 and 255
	 */
	public void setWindowBrightness(int brightnessLevel) {
		this.brightnessLevel = brightnessLevel;
		setWindowBrightnessImpl();
	}

	private void setWindowBrightnessImpl() {
		Window window = getWindow();
		if (window == null) {
			logWarning("Will not set brightness... no window");
			return;
		}

		LayoutParams lp = window.getAttributes();
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
		Activity activity = getActivity();
		if (activity == null)
			return ScreenOrientation.UNSPECIFIED;

		return ScreenOrientation.getOrientationByValue(activity.getRequestedOrientation());
	}

	/**
	 * @return 'Are we landscape or portrait?', for cases where getRequestedScreenOrientation returns e.g "FULL_USER" which doesn't tell you if the device is
	 * portrait/landscape.
	 */
	public boolean isLandscape() {
		return getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels;
	}

	public void keepScreenAwake(boolean toKeepScreenAwake) {
		this.stateKeepScreenAwake = toKeepScreenAwake;
		keepScreenAwakeImpl();
	}

	public void setFullscreen(boolean toSetFullScreen) {
		this.stateFullScreen = toSetFullScreen;
		setFullScreenImpl();
	}

	private void keepScreenAwakeImpl() {
		setScreenAwakeFlagsImpl();
		dismissKeyguardImpl();
	}

	private void dismissKeyguardImpl() {
		Activity activity = getActivity();
		if (activity == null) {
			logWarning("Will not dismiss keyguard... no activity");
			return;
		}

		if (VERSION.SDK_INT >= VERSION_CODES.O) // Setting the flags is enough for api 25 and below.
			getSystemService(KeyguardService).requestDismissKeyguard(activity, null);
	}

	private void setScreenAwakeFlagsImpl() {
		Window window = getWindow();
		if (window == null) {
			logWarning("Will not set screen awake state... no window");
			return;
		}

		if (this.stateKeepScreenAwake == null)
			return;

		if (this.stateKeepScreenAwake) {
			// As long as this window is visible to the user, keep the device's screen turned on and bright.
			window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

			// Since keyguard was dismissed all the time as long as an activity with this flag on its window was focused, keyguard couldn't guard against unintentional touches on the screen, which isn't desired. Deprecates at api 26
			window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);

			// Like FLAG_DISMISS_KEYGUARD - just deprecates at api 27, instead of 26. Deprecates at api 27
			window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);

			// When set as a window is being added or made visible, once the window has been shown then the system will poke the power manager's user activity (as if the user had woken up the device) to turn the screen on. Deprecates at api 27
			window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		} else {
			window.clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
			window.clearFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
			window.clearFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			window.clearFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
		}
	}

	private void setFullScreenImpl() {
		Window window = getWindow();
		if (window == null) {
			logWarning("Will not set full screen mode... no window");
			return;
		}

		if (this.stateFullScreen)
			window.addFlags(LayoutParams.FLAG_FULLSCREEN); // Hide all screen decorations (such as the status bar) while this window is displayed.
		else
			window.clearFlags(LayoutParams.FLAG_FULLSCREEN);
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
		SENSOR_PORTRAIT(SCREEN_ORIENTATION_SENSOR_PORTRAIT),
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
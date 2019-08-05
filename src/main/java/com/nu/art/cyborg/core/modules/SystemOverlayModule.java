package com.nu.art.cyborg.core.modules;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgController;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.CyborgView;

import static android.view.WindowManager.LayoutParams;

@SuppressWarnings("WeakerAccess")
@ModuleDescriptor(usesPermissions = permission.SYSTEM_ALERT_WINDOW)
public class SystemOverlayModule
	extends CyborgModule {

	@Override
	protected void init() {}

	public <ControllerType extends CyborgController> ControllerType showOverlay(Class<ControllerType> controllerType) {
		int type;
		if (VERSION.SDK_INT >= VERSION_CODES.O) {
			type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		} else {
			type = WindowManager.LayoutParams.TYPE_PHONE;
		}

		LayoutParams params = new LayoutParams(type, LayoutParams.FLAG_FULLSCREEN, PixelFormat.RGBA_8888);
		params.gravity = Gravity.START | Gravity.TOP;
		return showOverlay(controllerType, params);
	}

	@SuppressWarnings("unchecked")
	public <ControllerType extends CyborgController> ControllerType showOverlay(Class<ControllerType> controllerType, LayoutParams params) {
		CyborgView view = new CyborgView(getApplicationContext(), controllerType);
		getSystemService(WindowService).addView(view, params);
		return (ControllerType) view.getController();
	}

	public boolean requestDrawOverOtherAppsPermissionIfNeeded(Activity activity) {
		if (VERSION.SDK_INT >= VERSION_CODES.M) {
			if (!Settings.canDrawOverlays(activity)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
				activity.startActivityForResult(intent, 0);
				return false;
			}
		}

		return true;
	}
}

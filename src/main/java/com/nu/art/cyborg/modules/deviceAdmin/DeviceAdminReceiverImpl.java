package com.nu.art.cyborg.modules.deviceAdmin;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class DeviceAdminReceiverImpl
	extends DeviceAdminReceiver {

	private String TAG = DeviceAdminReceiverImpl.class.getSimpleName();

	public static ComponentName getComponentName(Context context) {
		return new ComponentName(context, DeviceAdminReceiverImpl.class);
	}

	@Override
	public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
		super.onLockTaskModeEntering(context, intent, pkg);
		Log.d(TAG, "onLockTaskModeEntering");
	}

	@Override
	public void onLockTaskModeExiting(Context context, Intent intent) {
		super.onLockTaskModeExiting(context, intent);
		Log.d(TAG, "onLockTaskModeExiting");
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		Toast.makeText(context, "DeviceAdmin ENABLED", Toast.LENGTH_SHORT).show();
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		return "You are about to remove the DeviceAdmin title from Kaspero.";
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		Toast.makeText(context,"DeviceAdmin DISABLED", Toast.LENGTH_SHORT).show();
	}
}
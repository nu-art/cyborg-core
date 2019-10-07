package com.nu.art.cyborg.modules.deviceAdmin;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.CyborgModule;

public class DeviceAdminReceiverImpl
	extends DeviceAdminReceiver {

	private String TAG = DeviceAdminReceiverImpl.class.getSimpleName();

	public static ComponentName getComponentName(Context context) {
		return new ComponentName(context, DeviceAdminReceiverImpl.class);
	}

	private <ModuleClass extends CyborgModule> ModuleClass getModule(Class<ModuleClass> moduleClass) {
		return CyborgBuilder.getInstance().getModule(moduleClass);
	}

	@Override
	public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
		getModule(DeviceAdminModule.class).onLockTaskModeEntering(intent, pkg);
	}

	@Override
	public void onLockTaskModeExiting(Context context, Intent intent) {
		getModule(DeviceAdminModule.class).onLockTaskModeExiting(intent);
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		getModule(DeviceAdminModule.class).onEnabled(intent);
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		getModule(DeviceAdminModule.class).onDisabled(intent);
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) {
		return getModule(DeviceAdminModule.class).getOnDisableRequestWarningMessage(intent);
	}
}
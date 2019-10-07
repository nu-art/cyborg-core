package com.nu.art.cyborg.modules.deviceAdmin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.preference.PreferenceManager.OnActivityResultListener;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ExceptionTools;
import com.nu.art.cyborg.core.CyborgActivity;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.modular.core.ValidationResult;

import java.util.List;

public class DeviceAdminModule
	extends CyborgModule
	implements OnActivityResultListener {

	public void onEnabled(Intent intent) {
		dispatchAdminEvent("DeviceAdmin/DeviceOwner enabled", new Processor<OnDeviceAdminResultListener>() {
			@Override
			public void process(OnDeviceAdminResultListener listener) {
				listener.onDeviceAdminEnabled();
			}
		});
	}

	public void onLockTaskModeExiting(Intent intent) {
		dispatchAdminEvent("Exiting LockTaskMode", new Processor<OnDeviceAdminResultListener>() {
			@Override
			public void process(OnDeviceAdminResultListener listener) {
				listener.onLockTaskModeExiting();
			}
		});
	}

	public void onLockTaskModeEntering(Intent intent, String pkg) {
		dispatchAdminEvent("Entering LockTaskMode with app " + pkg, new Processor<OnDeviceAdminResultListener>() {
			@Override
			public void process(OnDeviceAdminResultListener listener) {
				listener.onLockTaskModeEntering();
			}
		});
	}

	public void onDisabled(Intent intent) {
		dispatchAdminEvent("DeviceAdmin/DeviceOwner enabled", new Processor<OnDeviceAdminResultListener>() {
			@Override
			public void process(OnDeviceAdminResultListener listener) {
				listener.onDeviceAdminDisabled();
			}
		});
	}

	public void dispatchAdminEvent(String s, Processor<OnDeviceAdminResultListener> processor) {
		dispatchGlobalEvent(s, OnDeviceAdminResultListener.class, processor);
	}

	public String getOnDisableRequestWarningMessage(Intent intent) {
		return deviceAdminDisableRequestWarning;
	}

	public interface OnDeviceAdminResultListener {

		void onDeviceAdminEnabled();

		void onDeviceAdminDisabled();

		void onLockTaskModeEntering();

		void onLockTaskModeExiting();
	}

	protected String deviceAdminDisableRequestWarning = "You are about to disable this app from being DeviceAdmin.";

	public static final int PM_ACTIVATION_REQUEST_CODE = getPositiveShortHashCode(DeviceAdminModule.class);
	private DevicePolicyManager dpm;
	private ComponentName adminComponent;

	@Override
	protected void init() {
		dpm = getSystemService(PolicyManagerService);
		adminComponent = DeviceAdminReceiverImpl.getComponentName(getApplicationContext());
	}

	@Override
	protected void validateModule(ValidationResult result) {
		super.validateModule(result);
		try {
			cyborg.enforceBroadcastReceiverInManifest(DeviceAdminReceiverImpl.class);
		} catch (Exception e) {
			result.addEntry(this, ExceptionTools.getStackTrace(e));
		}
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO: 2019-10-07  handle DeviceAdmin or not OnDeviceAdminResultListener
		return false;
	}

	public void setUserRestriction(String restriction, Boolean disallow) {
		if (disallow) {
			dpm.addUserRestriction(adminComponent, restriction);
		} else {
			dpm.clearUserRestriction(adminComponent, restriction);
		}
	}

	public boolean isDeviceOwner() {
		return dpm.isDeviceOwnerApp(getPackageName());
	}

	public boolean isDeviceAdmin() {
		return dpm.isAdminActive(adminComponent);
	}

	/**
	 * Returns a list of both DeviceOwner and DeviceAdmin appss.
	 */
	public List<ComponentName> getActiveAdmins() {
		return dpm.getActiveAdmins();
	}

	/**
	 * Raises a user prompt asking to be approved as a Device Admin.
	 * MUST get activity context. Application context will not do.
	 */
	public void requestOwnership(CyborgActivity activity, String message) {
		final Intent devManagerIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		devManagerIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
		devManagerIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, message);
		devManagerIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		activity.startActivityForResult(devManagerIntent, PM_ACTIVATION_REQUEST_CODE);
	}

	/**
	 * Removes this app from being a DeviceAdmin.
	 */
	public void unownDevice() {
		removeSelfAsDeviceOwner();
		removeSelfAsDeviceAdmin();
	}

	/**
	 * Check and remove DeviceAdmin
	 */
	public void removeSelfAsDeviceAdmin() {
		String packageName = getPackageName();
		if (!dpm.isAdminActive(adminComponent)) {
			logInfo("App %s is not DeviceAdmin, can't remove DeviceAdmin status from app %s.", packageName, packageName);
			return;
		}

		dpm.removeActiveAdmin(adminComponent);
		logInfo("App %s is no longer DeviceAdmin.", packageName);
	}

	/**
	 * Check and remove DeviceOwner
	 */
	@Deprecated
	public void removeSelfAsDeviceOwner() {
		String packageName = getPackageName();
		if (!dpm.isDeviceOwnerApp(packageName)) {
			logInfo("App %s is not DeviceOwner, can't remove DeviceOwner status from app %s.", packageName, packageName);
			return;
		}

		dpm.clearDeviceOwnerApp(packageName);
		logInfo("App %s is no longer DeviceOwner.", packageName);
	}

	public void setDeviceAdminDisableRequestWarning(String deviceAdminDisableRequestWarning) {
		this.deviceAdminDisableRequestWarning = deviceAdminDisableRequestWarning;
	}
}

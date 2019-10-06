package com.nu.art.cyborg.modules.deviceAdmin;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.UserManager;

import com.nu.art.cyborg.core.CyborgActivity;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.ui.ApplicationLauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DeviceAdminModule
	extends CyborgModule {

	private PolicyManager policyManager;

	@Override
	protected void init() {
		policyManager = new PolicyManager();
	}

	public PolicyManager getPolicyManager() {
		return policyManager;
	}

	public class PolicyManager {

		public static final int PM_ACTIVATION_REQUEST_CODE = 101;
		private DevicePolicyManager dpm;
		private ComponentName adminComponent;

		public PolicyManager() {
			dpm = getSystemService(PolicyManagerService);
			adminComponent = DeviceAdminReceiverImpl.getComponentName(getApplicationContext());
		}

		public void setUserRestriction(String restriction, Boolean disallow) {
			if (disallow) {
				dpm.addUserRestriction(adminComponent, restriction);
			} else {
				dpm.clearUserRestriction(adminComponent, restriction);
			}
		}

		public boolean isDeviceOwner() {
			boolean isDeviceOwner = dpm.isDeviceOwnerApp(getPackageName());
			logWarning("We are %s", (isDeviceOwner ? "the device owner" : "not the device owner :( :( :("));
			return isDeviceOwner;
		}

		/**
		 * Raises a user prompt asking to be approved as a Device Admin.
		 */
		public void requestOwnership(CyborgActivity activity) {
			final Intent devManagerIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			devManagerIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
			devManagerIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app belongs to Intuition Robotics and should not be installed or administered on any devices not belonging to Intuition Robotics.");
			devManagerIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			activity.startActivityForResult(devManagerIntent, PM_ACTIVATION_REQUEST_CODE);
		}

		/**
		 * Removes this app from being a DeviceAdmin.
		 */
		public void unownDevice() {
			if (dpm == null) {
				logError("!!!Failed determining if app is device admin/owner!!!");
				return;
			}
			String packageName = getPackageName();
			removeSelfAsDeviceOwner(packageName);
			removeSelfAsDeviceAdmin(packageName);
		}

		/**
		 * Check and remove DeviceAdmin
		 */
		public void removeSelfAsDeviceAdmin(String packageName) {
			boolean isActiveDeviceAdmin = dpm.isAdminActive(DeviceAdminReceiverImpl.getComponentName(getApplicationContext()));
			if (!isActiveDeviceAdmin) {
				logInfo("App %s is not DeviceAdmin, can't remove DeviceAdmin status from app %s.", packageName, packageName);
			} else {
				dpm.removeActiveAdmin(DeviceAdminReceiverImpl.getComponentName(getApplicationContext()));
				logInfo("App %s is no longer DeviceAdmin.", packageName);
			}
		}

		/**
		 * Check and remove DeviceOwner
		 */
		public void removeSelfAsDeviceOwner(String packageName) {
			boolean isDeviceOwner = dpm.isDeviceOwnerApp(packageName);
			if (!isDeviceOwner) {
				logInfo("App %s is not DeviceOwner, can't remove DeviceOwner status from app %s.", packageName, packageName);
			} else {
				dpm.clearDeviceOwnerApp(packageName);
				logInfo("App %s is no longer DeviceOwner.", packageName);
			}
		}
	}

	public static class InstallerImpl {

		public static boolean installPackage(Context context, InputStream in, String packageName)
			throws IOException {
			PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
			PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
			params.setAppPackageName(packageName);
			// set params
			int sessionId = packageInstaller.createSession(params);
			PackageInstaller.Session session = packageInstaller.openSession(sessionId);
			OutputStream out = session.openWrite("COSU", 0, -1);
			byte[] buffer = new byte[65536];
			int c;
			while ((c = in.read(buffer)) != -1) {
				out.write(buffer, 0, c);
			}
			session.fsync(out);
			in.close();
			out.close();

			session.commit(createIntentSender(context, sessionId));
			return true;
		}

		private static IntentSender createIntentSender(Context context, int sessionId) {
			IntentSender statusReceiver = null;
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1337111117, new Intent(context, ApplicationLauncher.class), PendingIntent.FLAG_UPDATE_CURRENT);
			return pendingIntent.getIntentSender();
		}
	}

	/**
	 * Implementation, delete probably
	 */
	private void setRestrictions(Boolean disallow) {
		policyManager.setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow);
		//		policyManager.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow);
		policyManager.setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow);
		policyManager.setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow);
		policyManager.setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow);
	}
}

package com.nu.art.cyborg.modules;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.nu.art.core.generics.Processor;
import com.nu.art.core.tools.ArrayTools;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.ActivityStack.ActivityStackAction;
import com.nu.art.cyborg.core.CyborgActivityBridge;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.interfaces.OnSystemPermissionsResultListener;

import java.util.ArrayList;

@ModuleDescriptor
public class PermissionModule
		extends CyborgModule
		implements OnSystemPermissionsResultListener {

	private static final int RequestCode_Permissions = 100;

	public interface PermissionResultListener {

		void onPermissionsRejected(String[] rejected);

		void onAllPermissionsGranted();
	}

	public final String[] getRejectedPermissions(String... requestedPermissions) {
		ArrayList<String> notGranted = new ArrayList<>();
		for (String permission : requestedPermissions) {
			if (!isGranted(permission))
				notGranted.add(permission);
		}

		return ArrayTools.asArray(notGranted, String.class);
	}

	private boolean isGranted(String permission) {
		return ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	protected void init() {

	}

	public void requestPermission(final String... permissions) {
		String[] rejectedPermissions = getRejectedPermissions(permissions);

		if (rejectedPermissions.length == 0) {
			dispatchEvent("All Permissions Granted", PermissionResultListener.class, new Processor<PermissionResultListener>() {
				@Override
				public void process(PermissionResultListener listener) {
					listener.onAllPermissionsGranted();
				}
			});
			return;
		}

		postActivityAction(new ActivityStackAction() {
			@Override
			public void execute(CyborgActivityBridge bridge) {
				bridge.addPermissionResultListener(PermissionModule.this);
				ActivityCompat.requestPermissions(bridge.getActivity(), permissions, RequestCode_Permissions);
			}
		});
	}

	@Override
	public boolean onPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case RequestCode_Permissions: {
				final ArrayList<String> rejected = new ArrayList<>();

				for (int i = 0; i < grantResults.length; i++) {
					if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
						rejected.add(permissions[i]);
					}
				}

				String message = "Permissions request: " + (grantResults.length - rejected.size()) + "/" + grantResults.length + " Granted";
				dispatchEvent(message, PermissionResultListener.class, new Processor<PermissionResultListener>() {
					@Override
					public void process(PermissionResultListener listener) {
						if (rejected.size() > 0)
							listener.onPermissionsRejected(ArrayTools.asArray(rejected, String.class));
						else
							listener.onAllPermissionsGranted();
					}
				});
				return true;
			}
		}
		return false;
	}
}

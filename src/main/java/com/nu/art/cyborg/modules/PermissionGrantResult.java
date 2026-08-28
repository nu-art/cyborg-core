package com.nu.art.cyborg.modules;

import android.content.pm.PackageManager;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.tools.ArrayTools;

import java.util.ArrayList;
import java.util.Map;

public final class PermissionGrantResult {

	public final int requestCode;

	public final String[] rejected;

	public PermissionGrantResult(int requestCode, String[] rejected) {
		this.requestCode = requestCode;
		this.rejected = rejected != null ? rejected : new String[0];
	}

	public boolean allGranted() {
		return rejected.length == 0;
	}

	public static PermissionGrantResult from(int requestCode, String[] permissions, int[] grantResults) {
		if (permissions == null || grantResults == null)
			throw new BadImplementationException("permissions and grantResults are required");

		ArrayList<String> rejected = new ArrayList<>();
		int count = Math.min(permissions.length, grantResults.length);
		for (int i = 0; i < count; i++) {
			if (grantResults[i] == PackageManager.PERMISSION_DENIED)
				rejected.add(permissions[i]);
		}

		return new PermissionGrantResult(requestCode, ArrayTools.asArray(rejected, String.class));
	}

	public static int[] grantResultsFromMap(String[] permissions, Map<String, Boolean> result) {
		if (permissions == null)
			throw new BadImplementationException("permissions are required");

		int[] grantResults = new int[permissions.length];
		for (int i = 0; i < permissions.length; i++) {
			boolean granted = result != null && Boolean.TRUE.equals(result.get(permissions[i]));
			grantResults[i] = granted ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
		}
		return grantResults;
	}
}

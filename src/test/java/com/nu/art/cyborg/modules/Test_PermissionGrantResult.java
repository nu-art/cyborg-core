package com.nu.art.cyborg.modules;

import android.content.pm.PackageManager;

import com.nu.art.core.exceptions.runtime.BadImplementationException;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Test_PermissionGrantResult {

	@Test
	public void from_AllGranted() {
		PermissionGrantResult result = PermissionGrantResult.from(7, new String[]{"a", "b"}, new int[]{
			PackageManager.PERMISSION_GRANTED,
			PackageManager.PERMISSION_GRANTED
		});
		assertEquals(7, result.requestCode);
		assertTrue(result.allGranted());
		assertEquals(0, result.rejected.length);
	}

	@Test
	public void from_CollectsDenied() {
		PermissionGrantResult result = PermissionGrantResult.from(3, new String[]{"a", "b", "c"}, new int[]{
			PackageManager.PERMISSION_GRANTED,
			PackageManager.PERMISSION_DENIED,
			PackageManager.PERMISSION_DENIED
		});
		assertFalse(result.allGranted());
		assertArrayEquals(new String[]{"b", "c"}, result.rejected);
	}

	@Test(expected = BadImplementationException.class)
	public void from_RejectsNullPermissions() {
		PermissionGrantResult.from(1, null, new int[]{PackageManager.PERMISSION_GRANTED});
	}

	@Test
	public void grantResultsFromMap_MissingIsDenied() {
		Map<String, Boolean> map = new HashMap<>();
		map.put("a", Boolean.TRUE);
		int[] results = PermissionGrantResult.grantResultsFromMap(new String[]{"a", "b"}, map);
		assertArrayEquals(new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_DENIED}, results);
	}

	@Test
	public void grantResultsFromMap_NullMapDeniesAll() {
		int[] results = PermissionGrantResult.grantResultsFromMap(new String[]{"a"}, null);
		assertArrayEquals(new int[]{PackageManager.PERMISSION_DENIED}, results);
	}

	@Test(expected = BadImplementationException.class)
	public void grantResultsFromMap_RejectsNullPermissions() {
		PermissionGrantResult.grantResultsFromMap(null, new HashMap<String, Boolean>());
	}
}

package com.nu.art.cyborg.core.interfaces;

public interface OnSystemPermissionsResultListener {

	boolean onPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
}

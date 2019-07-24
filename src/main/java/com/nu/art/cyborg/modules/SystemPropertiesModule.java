package com.nu.art.cyborg.modules;

import com.nu.art.core.exceptions.runtime.MUST_NeverHappenException;
import com.nu.art.core.exceptions.runtime.ThisShouldNotHappenException;
import com.nu.art.modular.core.Module;

import java.lang.reflect.Method;

public class SystemPropertiesModule
	extends Module {

	private Method set;
	private Method getString;
	private Method getBoolean;
	private Method getInt;
	private Method getLong;

	@Override
	protected void init() {
		try {
			Class<?> sp = Class.forName("android.os.SystemProperties");

			set = sp.getMethod("set", String.class, String.class);
			getString = sp.getMethod("get", String.class, String.class);
			getBoolean = sp.getMethod("getBoolean", String.class, boolean.class);
			getInt = sp.getMethod("getInt", String.class, int.class);
			getLong = sp.getMethod("getLong", String.class, long.class);
		} catch (Throwable e) {
			logError("Error mapping system properties apis", new MUST_NeverHappenException("cannot get system properties", e));
		}
	}

	public void set(String key, String value) {
		try {
			set.invoke(null, key, value);
		} catch (Exception e) {
			throw new ThisShouldNotHappenException("error setting property: " + key + " = " + value, e);
		}
	}

	public String get(String key) {
		return get(key, null);
	}

	public String get(String key, String defaultValue) {
		try {
			String value = (String) getString.invoke(null, key);
			return value == null ? defaultValue : value;
		} catch (Exception e) {
			throw new ThisShouldNotHappenException("error getting property: " + key, e);
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		try {
			return (Boolean) getBoolean.invoke(null, key, defaultValue);
		} catch (Exception e) {
			throw new ThisShouldNotHappenException("error getting boolean property: " + key, e);
		}
	}

	public int getInt(String key, int defaultValue) {
		try {
			return (Integer) getInt.invoke(null, key, defaultValue);
		} catch (Exception e) {
			throw new ThisShouldNotHappenException("error getting integer property: " + key, e);
		}
	}

	public long getLong(String key, long def) {
		try {
			return (Long) getLong.invoke(null, key, def);
		} catch (Exception e) {
			throw new ThisShouldNotHappenException("error getting long property: " + key, e);
		}
	}
}
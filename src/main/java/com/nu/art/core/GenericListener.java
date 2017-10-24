package com.nu.art.core;

/**
 * Created by tacb0ss on 22/10/2017.
 */

public interface GenericListener<Type> {

	void onSuccess(Type type);

	void onError(Throwable e);
}

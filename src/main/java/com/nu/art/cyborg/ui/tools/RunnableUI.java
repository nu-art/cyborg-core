package com.nu.art.cyborg.ui.tools;

import android.os.Handler;

import com.nu.art.cyborg.core.CyborgBuilder;
import com.nu.art.cyborg.core.modules.ThreadsModule;

/**
 * A convenient wrapper for runnable to be called on the main thread, without knowing which thread is going to run it.
 */
public abstract class RunnableUI
		implements Runnable {

	@Override
	public final void run() {
		Handler uiHandler = CyborgBuilder.getModule(ThreadsModule.class).getDefaultHandler(ThreadsModule.MainThread);
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				runOnUi();
			}
		});
	}

	protected abstract void runOnUi();
}

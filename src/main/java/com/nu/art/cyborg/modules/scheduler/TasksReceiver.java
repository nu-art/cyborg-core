package com.nu.art.cyborg.modules.scheduler;

import android.content.Intent;

import com.nu.art.cyborg.core.CyborgReceiver;

public final class TasksReceiver
		extends CyborgReceiver<TaskScheduler> {

	public TasksReceiver() {
		super(TaskScheduler.class);
	}

	@Override
	protected void onReceive(final Intent intent, final TaskScheduler taskScheduler) {
		taskScheduler.process(intent);
	}
}

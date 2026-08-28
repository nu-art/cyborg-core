package com.nu.art.cyborg.core.jobs;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public final class WorkManagerJobRunner
	implements CyborgJobRunner {

	private final Context context;

	public WorkManagerJobRunner(Context context) {
		this.context = context.getApplicationContext();
	}

	@Override
	public void enqueue(CyborgJobPlan plan) {
		Constraints.Builder constraints = new Constraints.Builder();
		if (plan.requireNetwork)
			constraints.setRequiredNetworkType(NetworkType.CONNECTED);

		Data input = new Data.Builder().putString(CyborgJobWorker.Extra_JobId, plan.uniqueName).build();
		OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CyborgJobWorker.class).setConstraints(constraints.build())
		                                                                                  .setInputData(input)
		                                                                                  .build();
		ExistingWorkPolicy policy = plan.persist ? ExistingWorkPolicy.KEEP : ExistingWorkPolicy.REPLACE;
		WorkManager.getInstance(context).enqueueUniqueWork(plan.uniqueName, policy, request);
	}
}

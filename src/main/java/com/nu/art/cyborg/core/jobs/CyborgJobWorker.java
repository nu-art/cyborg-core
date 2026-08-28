package com.nu.art.cyborg.core.jobs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nu.art.cyborg.core.CyborgBuilder;

public final class CyborgJobWorker
	extends Worker {

	public static final String Extra_JobId = "jobId";

	public CyborgJobWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		String jobId = getInputData().getString(Extra_JobId);
		if (jobId == null || jobId.length() == 0)
			return Result.failure();

		CyborgBuilder.getInstance().getModule(CyborgJobsModule.class).dispatchJob(jobId);
		return Result.success();
	}
}

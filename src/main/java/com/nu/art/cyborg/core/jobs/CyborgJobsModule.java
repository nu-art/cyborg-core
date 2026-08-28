package com.nu.art.cyborg.core.jobs;

import com.nu.art.core.exceptions.runtime.BadImplementationException;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.common.utils.BootStarterReceiver.OnBootCompletedListener;
import com.nu.art.cyborg.core.CyborgModule;

/**
 * Enqueue work that must survive process death (network-back, after reboot).
 * Do not start a data-sync foreground service from {@link OnBootCompletedListener} — enqueue a job instead.
 */
public class CyborgJobsModule
	extends CyborgModule
	implements OnBootCompletedListener {

	public static final String JobId_Boot = "cyborg.boot";

	private CyborgJobRunner runner;

	private CyborgJobsModule() {}

	@Override
	protected void init() {
		runner = new WorkManagerJobRunner(getApplicationContext());
	}

	public void setRunner(CyborgJobRunner runner) {
		if (runner == null)
			throw new BadImplementationException("CyborgJobRunner is required");

		this.runner = runner;
	}

	public void enqueue(CyborgJobSpec spec) {
		if (runner == null)
			throw new BadImplementationException("CyborgJobsModule is not initialized");

		runner.enqueue(CyborgJobPlan.from(spec));
	}

	public void dispatchJob(final String jobId) {
		dispatchGlobalEvent("Cyborg job: " + jobId, CyborgJobListener.class, new Processor<CyborgJobListener>() {
			@Override
			public void process(CyborgJobListener listener) {
				listener.onCyborgJob(jobId);
			}
		});
	}

	@Override
	public void onBootCompleted() {
		enqueue(new CyborgJobSpec(JobId_Boot));
	}
}

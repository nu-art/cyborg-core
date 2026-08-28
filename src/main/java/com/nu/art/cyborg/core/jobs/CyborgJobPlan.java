package com.nu.art.cyborg.core.jobs;

/**
 * OS-agnostic plan for a Cyborg job. WorkManager (or a test runner) executes this.
 */
public final class CyborgJobPlan {

	public final String uniqueName;

	public final boolean requireNetwork;

	public final boolean persist;

	public CyborgJobPlan(String uniqueName, boolean requireNetwork, boolean persist) {
		this.uniqueName = uniqueName;
		this.requireNetwork = requireNetwork;
		this.persist = persist;
	}

	public static CyborgJobPlan from(CyborgJobSpec spec) {
		return new CyborgJobPlan(spec.getId(), spec.isRequireNetwork(), spec.isPersist());
	}
}

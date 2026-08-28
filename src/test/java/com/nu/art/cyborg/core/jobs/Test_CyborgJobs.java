package com.nu.art.cyborg.core.jobs;

import com.nu.art.core.exceptions.runtime.BadImplementationException;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Test_CyborgJobs {

	@Test(expected = BadImplementationException.class)
	public void spec_RejectsEmptyId() {
		new CyborgJobSpec("");
	}

	@Test(expected = BadImplementationException.class)
	public void spec_RejectsNullId() {
		new CyborgJobSpec(null);
	}

	@Test
	public void plan_CopiesSpec() {
		CyborgJobSpec spec = new CyborgJobSpec("sync").requireNetwork().persist(false);
		CyborgJobPlan plan = CyborgJobPlan.from(spec);
		assertEquals("sync", plan.uniqueName);
		assertTrue(plan.requireNetwork);
		assertFalse(plan.persist);
	}

	@Test
	public void plan_DefaultsPersist() {
		CyborgJobPlan plan = CyborgJobPlan.from(new CyborgJobSpec("boot"));
		assertFalse(plan.requireNetwork);
		assertTrue(plan.persist);
	}

	@Test
	public void boot_EnqueuesJob()
		throws Exception {
		Constructor<CyborgJobsModule> ctor = CyborgJobsModule.class.getDeclaredConstructor();
		ctor.setAccessible(true);
		CyborgJobsModule module = ctor.newInstance();
		final CyborgJobPlan[] captured = new CyborgJobPlan[1];
		module.setRunner(new CyborgJobRunner() {
			@Override
			public void enqueue(CyborgJobPlan plan) {
				captured[0] = plan;
			}
		});
		module.onBootCompleted();
		assertEquals(CyborgJobsModule.JobId_Boot, captured[0].uniqueName);
		assertFalse(captured[0].requireNetwork);
	}

	@Test(expected = BadImplementationException.class)
	public void enqueue_RejectsMissingRunner()
		throws Exception {
		Constructor<CyborgJobsModule> ctor = CyborgJobsModule.class.getDeclaredConstructor();
		ctor.setAccessible(true);
		ctor.newInstance().enqueue(new CyborgJobSpec("x"));
	}
}

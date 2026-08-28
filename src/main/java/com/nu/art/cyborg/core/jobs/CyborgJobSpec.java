package com.nu.art.cyborg.core.jobs;

import com.nu.art.core.exceptions.runtime.BadImplementationException;

public final class CyborgJobSpec {

	private final String id;

	private boolean requireNetwork;

	private boolean persist = true;

	public CyborgJobSpec(String id) {
		if (id == null || id.length() == 0)
			throw new BadImplementationException("CyborgJobSpec id is required");

		this.id = id;
	}

	public CyborgJobSpec requireNetwork() {
		requireNetwork = true;
		return this;
	}

	public CyborgJobSpec persist(boolean persist) {
		this.persist = persist;
		return this;
	}

	public String getId() {
		return id;
	}

	public boolean isRequireNetwork() {
		return requireNetwork;
	}

	public boolean isPersist() {
		return persist;
	}
}

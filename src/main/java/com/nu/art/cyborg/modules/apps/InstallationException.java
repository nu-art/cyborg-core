package com.nu.art.cyborg.modules.apps;

public class InstallationException
	extends Exception {

	public InstallationException() {
	}

	public InstallationException(String message) {
		super(message);
	}

	public InstallationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InstallationException(Throwable cause) {
		super(cause);
	}
}

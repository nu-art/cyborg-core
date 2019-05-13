package com.nu.art.cyborg.tools;

public enum ResourceType {
	Layout("layout"),
	Id("id");

	private final String className;

	ResourceType(String name) {
		this.className = name;
	}

	public String getClassName() {
		return className;
	}

}

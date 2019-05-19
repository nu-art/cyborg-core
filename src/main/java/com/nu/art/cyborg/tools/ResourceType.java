package com.nu.art.cyborg.tools;

public enum ResourceType {
	Layout("layout"),
	Id("id");

	public final String type;

	ResourceType(String name) {
		this.type = name;
	}

	public String getClassName() {
		return type;
	}

}

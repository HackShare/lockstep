package com.picostuff.lockstep;

public class LocalItemInfo {
	private String name;
	private String version;
	
	public LocalItemInfo(String name, String version) {
		this.name = name;
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
}

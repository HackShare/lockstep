package com.picostuff.lockstep;

public class RemoteItemInfo {
	private String name;
	private String version;
	
	public RemoteItemInfo(String name, String version) {
		this.name = name;
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
}

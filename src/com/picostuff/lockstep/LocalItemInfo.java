package com.picostuff.lockstep;

public class LocalItemInfo {
	private String name;
	private String version;
	
	public LocalItemInfo(String name, String version) {
		this.name = name;
		if (version == null)
			version = SharedMemory.DIR_NODE_VERSION;
		this.version = version;
	}
	
	public LocalItemInfo(String name) {
		// a dir
		this(name,null);
	}
	
	public String getName() {
		return name;
	}
	public String getVersion() {
		return version;
	}
}

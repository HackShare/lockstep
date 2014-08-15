package com.picostuff.lockstep;

/**
 * This might end up the same as RemoteItemInfo
 * 
 * @author cear
 *
 */
public class BaseItemInfo {
	private String name;
	private String version;
	
	public BaseItemInfo(String name, String version) {
		this.name = name;
		this.version = version;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}

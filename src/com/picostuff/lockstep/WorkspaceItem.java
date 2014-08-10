package com.picostuff.lockstep;

/**
 * An item in the workspace that changes.  The version should be persisted as long as the item hasn't changed.
 * 
 * @author chenglim
 *
 */
public class WorkspaceItem {
	private String name;
	private String data;
	private boolean changed;
	private WorkspaceItem oldItem;
	
	public WorkspaceItem(String name, String data) {
		this.name = name;
		this.data = data;
		changed = true;
	}
	
	public String getVersion() {
		// for a file, we can return a sha1 of the file content, but with this test fixture, we will just be returning the data
		if (data == null) {
			return SharedMemory.DIR_NODE_VERSION;
		}
		return data;
	}
	
	public void setOldItem(WorkspaceItem oldItem) {
		this.oldItem = oldItem;
	}
	
	public void markUpdated() {
		changed = false;
		oldItem = null;
	}
	
	public boolean isChanged() {
		return changed;
	}
}

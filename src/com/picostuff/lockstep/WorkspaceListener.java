package com.picostuff.lockstep;

public interface WorkspaceListener {
	public void itemAddedLocally(String key);
	public void itemChangedLocally(String key, WorkspaceItem oldItem);
}

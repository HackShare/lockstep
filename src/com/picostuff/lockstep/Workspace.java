package com.picostuff.lockstep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.picostuff.lockstep.exception.BadPathException;
import com.picostuff.lockstep.exception.SaveConflictException;

/**
 * Eventually, this will be an interface fronting different
 * implementations of a workspace, which is typically a local
 * directory
 * 
 * @author chenglim
 *
 */
public class Workspace {
	private Map<String, WorkspaceItem> items; // TODO: this should be persisted so that we can recover/continue after quitting
	private List<WorkspaceListener> listeners;
	
	public Workspace() {
		items = new HashMap<String, WorkspaceItem>();
		listeners = new ArrayList<WorkspaceListener>();
	}
	
	public void addWorkspaceListener(WorkspaceListener listener) {
		listeners.add(listener);
	}
	
	public void refresh() throws BadPathException, SaveConflictException {
		// This is when events of local changes are processed
		// We want to keep this from running too long, so maybe a separate thread to look at files and queue updates
		// Being able to watch for changes asynchronously instead of polling would be a plus.
		refreshLocalItemAndParents("/a/b", "mydata");
	}
	
	public WorkspaceItem getItem(String key) {
		return items.get(key);
	}
	
	// This is used to find item changes to add to shared memory
	public List<String> findNextChangedItems(int max) {
		// TODO: this can definitely be optimized - maybe maintain an index to changed items
		int count = 0;
		List<String> changedItems = new ArrayList<String>();
		for (String key:items.keySet()) {
			if (items.get(key).isChanged()) {
				changedItems.add(key);
				count++;
				if (count >= max)
					break;
			}
		}
		return changedItems;
	}

	// called for local changes
	public void refreshLocalItemAndParents(String key, String data) throws BadPathException, SaveConflictException {
		// TODO: data might just be a pointer to a file
		WorkspaceItem oldItem = items.get(key);
		String[] parts = makePathParts(key);
		addDirs(parts,false);
		String name = parts[parts.length - 1];
		WorkspaceItem item = new WorkspaceItem(name,data);
		if (oldItem == null) {
			// first time update
			items.put(key,item);
			for (WorkspaceListener listener:listeners) {
				listener.itemAddedLocally(key);
			}
			
		} else {
			if (!item.getVersion().equals(oldItem.getVersion())) {
				items.put(key, item);
				for (WorkspaceListener listener:listeners) {
					listener.itemChangedLocally(key, oldItem);
				}
			}
		}
	}
	
	public void rejectLocalItem(String key) {
		// TODO: save rejected files somehow (maybe) and maybe stop syncing when n rejects exist
		// TODO: remove/move file
		items.remove(key);
	}

	// called for remote add
	public void addRemoteItemAndParents(String key, String data) throws BadPathException, SaveConflictException {
		WorkspaceItem oldItem = items.get(key);
		boolean potentialConflict = (oldItem != null);
		if (potentialConflict) {
			processRemoteItemPotentialConflict(key, data, oldItem);
		}
		processRemoteItemAndParentsChange(key, data);
		
	}

	// called for remote change
	public void updateRemoteItemAndParents(String key, String data, String oldVersion) throws BadPathException, SaveConflictException {
		WorkspaceItem oldItem = items.get(key);
		boolean potentialConflict = (oldItem != null) && (oldItem.isChanged() || !oldItem.getVersion().equals(oldVersion));
		if (potentialConflict) {
			processRemoteItemPotentialConflict(key, data, oldItem);
		}
		processRemoteItemAndParentsChange(key, data);
	}
	
	// called for remote delete
	public void deleteRemoteItem(String key, String oldVersion) {
		// TODO: look for potential conflict before processing
	}

	public void processRemoteItemAndParentsChange(String key, String data) throws BadPathException, SaveConflictException {
		String[] parts = makePathParts(key);
		String name = parts[parts.length - 1];
		WorkspaceItem item = new WorkspaceItem(name,data);
		addDirs(parts, true);
		item.markUpdated(); // this is an update from remote
		items.put(key,item);
	}

	public void processRemoteItemPotentialConflict(String key, String data, WorkspaceItem oldItem) throws BadPathException, SaveConflictException {
		String[] parts = makePathParts(key);
		String name = parts[parts.length - 1];
		WorkspaceItem item = new WorkspaceItem(name,data);
		// we're changing something that's not an old remote version
		if (item.getVersion().equals(oldItem.getVersion())) {
			// the local change matched the remote change
			oldItem.markUpdated();
			return;
		} else {
			// we're updating the wrong version or add when something's there, 
			// so it's a conflict that needs resolving -- we must reject it first
			throw new SaveConflictException();
		}
	}

	private String[] makePathParts(String path) throws BadPathException {
		String[] parts = path.split("/",-1);
		if ((parts.length < 2) || !parts[0].equals("") || parts[1].equals(""))
			throw new BadPathException(path);
		return parts;
	}

	private void addDirs(String[] parts, boolean updated) throws SaveConflictException {
		StringBuilder dirPath = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			if (!parts[i].equals("")) {
				// add a dir
				dirPath.append("/").append(parts[i]);
				WorkspaceItem item = new WorkspaceItem(parts[i],null);
				String key = dirPath.toString();
				if (updated)
					item.markUpdated(); // this is an update from remote
				else {
					// for local adds, we call listeners
					WorkspaceItem oldItem = items.get(key);
					if (oldItem == null) {
						// new dir
						for (WorkspaceListener listener:listeners) {
							listener.itemAddedLocally(key);
						}
					}
				}
				items.put(key,item);
			}
		}
		
	}
	
}

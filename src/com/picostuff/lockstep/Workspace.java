package com.picostuff.lockstep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

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
	
	private Map<String,String> fileSystem; // simulate files in file system
	private Map<String, BaseItemInfo> lastUpdatedItems; // TODO: this should be persisted across process restarts
	
	public Workspace() {
		fileSystem = new HashMap<String, String>();
		lastUpdatedItems = new HashMap<String, BaseItemInfo>();
	}
	
	public void initFileSet() {
		fileSystem.put("/a", null);
		fileSystem.put("/a/b", "mydata");
	}
	
	public Set<String> getFileSet() {
		Set<String> set = new TreeSet<String>();
		set.addAll(fileSystem.keySet());
		return set;
	}
	
	public void processItem(String key, RemoteItemInfo remoteInfo) throws SaveConflictException, BadPathException {
		BaseItemInfo baseInfo = lastUpdatedItems.get(key);
		LocalItemInfo localInfo = getLocalInfo(key);
		RemoteItemState remoteItemState = RemoteItemState.findState(remoteInfo, baseInfo);
		LocalItemState localItemState = LocalItemState.findState(localInfo, baseInfo);
		switch (remoteItemState) {
		case REMOTE_CHANGED:
			switch (localItemState) {
			case LOCAL_UNCHANGED:
				updateLocalItem(key,remoteInfo, baseInfo);
				break;
			case LOCAL_NOTHING:
				updateLocalItem(key,remoteInfo, baseInfo);
				break;
			}
			break;
		case REMOTE_NEW:
			switch (localItemState) {
			case LOCAL_UNCHANGED:
				updateLocalItem(key,remoteInfo, baseInfo);
				break;
			case LOCAL_NOTHING:
				updateLocalItem(key,remoteInfo, baseInfo);
				break;
			case LOCAL_NEW:
				if (localInfo.getVersion().equals(remoteInfo.getVersion())) {
					// local is the same as new, so we just need to update our base info
					updateBaseInfo(key, remoteInfo, baseInfo);
				} else {
					throw new SaveConflictException();
				}
			}
			break;
		default:
			throw new RuntimeException("Unexpected remote state");  // We should never get here, so that's why it's so drastic
		}
	}
	
	private LocalItemInfo getLocalInfo(String key) throws BadPathException {
		String content = fileSystem.get(key); // content is version for now
		if (content != null) {
			String[] parts = makePathParts(key);
			String name = parts[parts.length - 1];
			LocalItemInfo localInfo = new LocalItemInfo(name,content);
			return localInfo;
		} else {
			return null;
		}
	}
	
	private void updateBaseInfo(String key, RemoteItemInfo remoteInfo, BaseItemInfo baseInfo) {
		if (baseInfo == null) {
			baseInfo = new BaseItemInfo(remoteInfo.getName(),remoteInfo.getVersion());
		} else {
			baseInfo.setVersion(remoteInfo.getVersion());
		}
		lastUpdatedItems.put(key, baseInfo);
	}
	private void updateLocalItem(String key, RemoteItemInfo remoteInfo, BaseItemInfo baseInfo) throws BadPathException, SaveConflictException {
		String[] parts = makePathParts(key);
		addDirs(parts);
		fileSystem.put(key, remoteInfo.getVersion()); // In the real situation, we might have a link to the data and transfer it to the file system
		updateBaseInfo(key,remoteInfo,baseInfo);
	}
	
	public void rejectLocalItem(String key) {
		// TODO: save rejected files somehow (maybe) and maybe stop syncing when n rejects exist
		// TODO: remove/move file
		// TODO: consider the case where a parent dir is actually a file and so the file does not actually exist.  That parent file needs to be
		// the one removed
		fileSystem.remove(key);
	}

	private String[] makePathParts(String path) throws BadPathException {
		String[] parts = path.split("/",-1);
		if ((parts.length < 2) || !parts[0].equals("") || parts[1].equals(""))
			throw new BadPathException(path);
		return parts;
	}

	private void addDirs(String[] parts) throws SaveConflictException {
		// this adds to the file system to allow adding new files, but will be reconciled with the distributed system later
		StringBuilder dirPath = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			if (!parts[i].equals("")) {
				// add a dir
				dirPath.append("/").append(parts[i]);
				String key = dirPath.toString();
				String content = fileSystem.get(key);
				if (content == null) {
					fileSystem.put(key, null);
				} else {
					throw new SaveConflictException();
				}
			}
		}
	}
		
}

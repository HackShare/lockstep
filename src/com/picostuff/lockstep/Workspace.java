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
	
	public Workspace(Map<String,String> fileSystem) {
		this.fileSystem = fileSystem;
		lastUpdatedItems = new HashMap<String, BaseItemInfo>();
	}
	
	public Set<String> getFileSet() {
		Set<String> set = new TreeSet<String>();
		set.addAll(fileSystem.keySet());
		return set;
	}
	
	public LocalItemInfo processItem(String key, RemoteItemInfo remoteInfo) throws SaveConflictException, BadPathException {
		// return a local item if we it needs to be processed as an add
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
			case LOCAL_CHANGED:
				if (localInfo.getVersion().equals(remoteInfo.getVersion())) {
					// local is the same as new, so we just need to update our base info
					updateBaseInfo(key, remoteInfo, baseInfo);
				} else {
					throw new SaveConflictException();
				}
				break;
			case LOCAL_NOTHING:
				updateLocalItem(key,remoteInfo, baseInfo);
				break;
			default:
				throw new RuntimeException("Unexpected local state: " + localItemState);  // We should never get here, so that's why it's so drastic
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
				break;
			default:
				throw new RuntimeException("Unexpected local state: " + localItemState);  // We should never get here, so that's why it's so drastic
			}
			break;
		case REMOTE_NOTHING:
			switch (localItemState) {
			case LOCAL_NEW:
				return localInfo;
			case LOCAL_NOTHING:
				// make sure our base info is up to date
				removeBaseInfo(key);
				break;
			default:
				throw new RuntimeException("Unexpected local state: " + localItemState);  // We should never get here, so that's why it's so drastic
			}
			break;
		case REMOTE_UNCHANGED:
			switch (localItemState) {
			case LOCAL_CHANGED:
				return localInfo;
			default:
				throw new RuntimeException("Unexpected local state: " + localItemState);  // We should never get here, so that's why it's so drastic
			}
			//break;
		case REMOTE_DELETED:
			switch (localItemState) {
			case LOCAL_UNCHANGED:
				removeLocalItem(key);
				break;
			case LOCAL_CHANGED:
				throw new SaveConflictException();
			default:
				throw new RuntimeException("Unexpected local state: " + localItemState);  // We should never get here, so that's why it's so drastic
			}
			break;
		default:
			throw new RuntimeException("Unexpected remote state: " + remoteItemState);  // We should never get here, so that's why it's so drastic
		}
		return null;
	}
	
	private LocalItemInfo getLocalInfo(String key) throws BadPathException {
		if (fileSystem.containsKey(key)) {
			String content = fileSystem.get(key); // content is version for now
			String[] parts = makePathParts(key);
			String name = parts[parts.length - 1];
			return new LocalItemInfo(name,content); // null content is dir
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
		fileSystem.remove(key); // in reality, we maybe rename to a rejected filename
		lastUpdatedItems.remove(key); // the file is considered last deleted (since we can't recover what it was before and so we can accept whatever it currently is remotely)
	}
	
	public void removeLocalItem(String key) {
		fileSystem.remove(key);
		removeBaseInfo(key);
	}
	
	private void removeBaseInfo(String key) {
		lastUpdatedItems.remove(key);
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

package com.picostuff.lockstep;

public enum RemoteItemState {
	REMOTE_CHANGED, REMOTE_NEW, REMOTE_DELETED, REMOTE_UNCHANGED, REMOTE_NOTHING;
	
	public static RemoteItemState findState(RemoteItemInfo remoteInfo, BaseItemInfo baseInfo) {
		if (remoteInfo == null) {
			if (baseInfo == null) {
				return REMOTE_NOTHING;
			} else {
				return REMOTE_DELETED;
			}
		} else {
			if (baseInfo == null) {
				return REMOTE_NEW;
			} else if (!remoteInfo.getVersion().equals(baseInfo.getVersion())) {
				return REMOTE_CHANGED;
			} else {
				return REMOTE_UNCHANGED;
			}
		}
	}
}

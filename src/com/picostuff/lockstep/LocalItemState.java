package com.picostuff.lockstep;

/**
 * This may just be the same as RemoteItemState
 * @author cear
 *
 */
public enum LocalItemState {
	LOCAL_CHANGED, LOCAL_NEW, LOCAL_DELETED, LOCAL_UNCHANGED, LOCAL_NOTHING;
	
	public static LocalItemState findState(LocalItemInfo localInfo, BaseItemInfo baseInfo) {
		if (localInfo == null) {
			if (baseInfo == null) {
				return LOCAL_NOTHING;
			} else {
				return LOCAL_DELETED;
			}
		} else {
			if (baseInfo == null) {
				return LOCAL_NEW;
			} else if (!localInfo.getVersion().equals(baseInfo.getVersion())) {
				return LOCAL_CHANGED;
			} else {
				return LOCAL_UNCHANGED;
			}
		}
	}
}

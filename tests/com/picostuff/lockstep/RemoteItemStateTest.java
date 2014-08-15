package com.picostuff.lockstep;

import junit.framework.Assert;

import org.junit.Test;

public class RemoteItemStateTest {

	@Test
	public void findRemoteUnchanged() {
		RemoteItemInfo remoteInfo = new RemoteItemInfo("name","version1");
		BaseItemInfo baseInfo = new BaseItemInfo("name","version1");
		RemoteItemState state = RemoteItemState.findState(remoteInfo,baseInfo);
		Assert.assertSame(RemoteItemState.REMOTE_UNCHANGED, state);
	}

	@Test
	public void findRemoteChanged() {
		RemoteItemInfo remoteInfo = new RemoteItemInfo("name","version1");
		BaseItemInfo baseInfo = new BaseItemInfo("name","version2");
		RemoteItemState state = RemoteItemState.findState(remoteInfo,baseInfo);
		Assert.assertSame(RemoteItemState.REMOTE_CHANGED, state);
	}

	@Test
	public void findRemoteNew() {
		RemoteItemInfo remoteInfo = new RemoteItemInfo("name","version1");
		RemoteItemState state = RemoteItemState.findState(remoteInfo,null);
		Assert.assertSame(RemoteItemState.REMOTE_NEW, state);
	}

	@Test
	public void findRemoteDeleted() {
		BaseItemInfo baseInfo = new BaseItemInfo("name","version1");
		RemoteItemState state = RemoteItemState.findState(null,baseInfo);
		Assert.assertSame(RemoteItemState.REMOTE_DELETED, state);
	}

	@Test
	public void findRemoteNothing() {
		RemoteItemState state = RemoteItemState.findState(null,null);
		Assert.assertSame(RemoteItemState.REMOTE_NOTHING, state);
	}

}

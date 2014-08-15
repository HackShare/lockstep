package com.picostuff.lockstep;

import junit.framework.Assert;

import org.junit.Test;

public class LocalItemStateTest {

	@Test
	public void findLocalUnchanged() {
		LocalItemInfo localInfo = new LocalItemInfo("name","version1");
		BaseItemInfo baseInfo = new BaseItemInfo("name","version1");
		LocalItemState state = LocalItemState.findState(localInfo,baseInfo);
		Assert.assertSame(LocalItemState.LOCAL_UNCHANGED, state);
	}

	@Test
	public void findLocalChanged() {
		LocalItemInfo localInfo = new LocalItemInfo("name","version1");
		BaseItemInfo baseInfo = new BaseItemInfo("name","version2");
		LocalItemState state = LocalItemState.findState(localInfo,baseInfo);
		Assert.assertSame(LocalItemState.LOCAL_CHANGED, state);
	}

	@Test
	public void findLocalNew() {
		LocalItemInfo localInfo = new LocalItemInfo("name","version1");
		LocalItemState state = LocalItemState.findState(localInfo,null);
		Assert.assertSame(LocalItemState.LOCAL_NEW, state);
	}

	@Test
	public void findLocalDeleted() {
		BaseItemInfo baseInfo = new BaseItemInfo("name","version1");
		LocalItemState state = LocalItemState.findState(null,baseInfo);
		Assert.assertSame(LocalItemState.LOCAL_DELETED, state);
	}

	@Test
	public void findLocalNothing() {
		LocalItemState state = LocalItemState.findState(null,null);
		Assert.assertSame(LocalItemState.LOCAL_NOTHING, state);
	}

}

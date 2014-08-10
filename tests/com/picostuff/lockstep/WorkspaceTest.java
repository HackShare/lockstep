package com.picostuff.lockstep;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picostuff.lockstep.exception.SaveConflictException;

/**
 * Test to drive a workspace interface
 * 
 * @author chenglim
 *
 */
public class WorkspaceTest {
	private Workspace workspace;
	private List<String> keysAdded;
	private List<String> keysChanged;

	@Before
	public void setup() throws Exception {
		workspace = new Workspace(); // "/a/b" => "mydata"
		keysAdded = new ArrayList<String>();
		keysChanged = new ArrayList<String>();
		workspace.addWorkspaceListener(new WorkspaceListener() {
			
			@Override
			public void itemChangedLocally(String key, WorkspaceItem oldItem) {
				keysChanged.add(key);
			}
			
			@Override
			public void itemAddedLocally(String key) {
				keysAdded.add(key);
			}
		});
	}

	@After
	public void teardown() throws Exception {
		workspace = null;
	}

	@Test
	public void startup() throws Exception {
		workspace.refresh(); // load info about files // TODO: make this async?
		Assert.assertEquals(0, keysChanged.size());
		Assert.assertEquals(2, keysAdded.size());
		Assert.assertEquals("/a",keysAdded.get(0));
		Assert.assertEquals("/a/b",keysAdded.get(1));
		WorkspaceItem item = workspace.getItem("/a");
		Assert.assertTrue(item.isChanged());
		item = workspace.getItem("/a/b");
		Assert.assertTrue(item.isChanged());
		// all items should be changed when first added
	}

	@Test
	public void addItemNoConflict() throws Exception {
		workspace.refresh();
		
		// simulate an simultaneous add coming in, which can occur if we start sync without track last remote
		// (which means all remote copy of items are treated as a new add
		workspace.addRemoteItemAndParents("/a/b", "mydata"); // data is the same, so there's no conflict
		Assert.assertEquals(0, keysChanged.size());
		Assert.assertEquals(2, keysAdded.size());
		Assert.assertEquals("/a",keysAdded.get(0));
		Assert.assertEquals("/a/b",keysAdded.get(1));
		WorkspaceItem item = workspace.getItem("/a");
		Assert.assertTrue("check item is not changed", !item.isChanged());
		item = workspace.getItem("/a/b");
		Assert.assertTrue("check item is not changed", !item.isChanged());
		
		// now it is synced up, so a change in data will not result in conflict
		workspace.updateRemoteItemAndParents("/a/b", "mynewdata",item.getVersion());
		item = workspace.getItem("/a/b");
		Assert.assertTrue("check item is not changed", !item.isChanged());
	}

	@Test
	public void addItemWithConflict() throws Exception {
		workspace.refresh();
		
		// simulate an simultaneous add coming in, which can occur if we start sync without track last remote
		// (which means all remote copy of items are treated as a new add
		try {
			workspace.addRemoteItemAndParents("/a/b", "mynewdata"); // data is new, so there's conflict
			fail("Did not have save conflict");
		} catch (SaveConflictException e) {
			// on save conflict, we want to reject item and try again
			workspace.rejectLocalItem("/a/b");
			workspace.addRemoteItemAndParents("/a/b", "mynewdata"); // try again
		}
		Assert.assertEquals(0, keysChanged.size());
		Assert.assertEquals(2, keysAdded.size());
		Assert.assertEquals("/a",keysAdded.get(0));
		Assert.assertEquals("/a/b",keysAdded.get(1));
		WorkspaceItem item = workspace.getItem("/a");
		Assert.assertTrue("check item is not changed", !item.isChanged());
		item = workspace.getItem("/a/b");
		Assert.assertTrue("check item is not changed", !item.isChanged());
	}

}

package com.picostuff.lockstep;

import static org.junit.Assert.*;

import java.util.Set;

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

	@Before
	public void setup() throws Exception {
		workspace = new Workspace(); 
		workspace.initFileSet(); // "/a/b" => "mydata"
		
	}

	@After
	public void teardown() throws Exception {
		workspace = null;
	}

	@Test
	public void simultaneousAddNoConflict() throws Exception {
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		
		// simulate a simultaneous add coming in, which can occur if we start sync without tracking last remote
		// (which means all remote copy of items are treated as a new add
		RemoteItemInfo remoteInfo = new RemoteItemInfo("b","mydata");
		workspace.processItem("/a/b", remoteInfo); // data is the same, so there's no conflict
		fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
		
		// now it is synced up, so a change in data will not result in conflict
		remoteInfo = new RemoteItemInfo("b","mynewdata");
		workspace.processItem("/a/b", remoteInfo); 
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));

	}

	@Test
	public void simultaneousAddWithConflict() throws Exception {
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		
		// simulate a simultaneous add coming in, which can occur if we start sync without tracking last remote
		// (which means all remote copy of items are treated as a new add
		RemoteItemInfo remoteInfo = new RemoteItemInfo("b","mynewdata");
		try {
			workspace.processItem("/a/b", remoteInfo); // data is different, so we expect a conflict
			fail("Did not have save conflict");
		} catch (SaveConflictException e) {
			// in a conflict, we have to resolve the situation and the simplest is to reject the local copy
			workspace.rejectLocalItem("/a/b");
			workspace.processItem("/a/b", remoteInfo); // do again
		}
		fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
		
	}
	
	@Test
	public void simultaneousAddWithDirAsLocalFileConflict() {
		fail("Not implemented");
	}

}

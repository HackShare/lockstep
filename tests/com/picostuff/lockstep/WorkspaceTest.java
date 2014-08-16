package com.picostuff.lockstep;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picostuff.lockstep.exception.BadPathException;
import com.picostuff.lockstep.exception.SaveConflictException;

/**
 * Test to drive a workspace interface
 * 
 * @author chenglim
 *
 */
public class WorkspaceTest {
	private Map<String,String> fileSystem;
	private Workspace workspace;

	@Before
	public void setup() throws Exception {
		fileSystem = new HashMap<String, String>();
		fileSystem.put("/a", null);
		fileSystem.put("/a/b", "mydata");
		workspace = new Workspace(fileSystem); 
		
		// Verify Initial State: Used to fail early so that we don't waste time running our tests
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size()); // our initial test state
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
	}

	@After
	public void teardown() throws Exception {
		workspace = null;
	}
	
	private void syncNewWorkspace() throws SaveConflictException, BadPathException {
		Set<String> fileSet = workspace.getFileSet();
		for (String key:fileSet) {
			LocalItemInfo localInfo = workspace.processItem(key, null);
			if (localInfo != null) {
				RemoteItemInfo remoteInfo = new RemoteItemInfo(localInfo.getName(), localInfo.getVersion());
				workspace.processItem(key, remoteInfo);
			}
		}
	}
	
	@Test
	public void simultaneousAddNoConflict() throws Exception {
		// simulate a simultaneous add coming in, which can occur if we start sync without tracking last remote
		// (which means all remote copy of items are treated as a new add
		RemoteItemInfo remoteInfo = new RemoteItemInfo("b","mydata");
		workspace.processItem("/a/b", remoteInfo); // data is the same, so there's no conflict
		Set<String> fileSet = workspace.getFileSet();
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
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
		
	}
	
	@Test
	public void simultaneousAddWithDirAsLocalFileConflict() {
		fail("Not implemented");
	}
	
	@Test
	public void localAddOnly() throws SaveConflictException, BadPathException {
		// no remote and local add only should result in remote updated
		String key = "/a/b";
		LocalItemInfo localInfo = workspace.processItem(key, null); // no remote data results in opening up processing of local add
		Assert.assertNotNull("check local info retuned", localInfo);
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
		
		// after getting local info, we will want to update the remote info and then re-process with the new remote info
		RemoteItemInfo remoteInfo = new RemoteItemInfo(localInfo.getName(), localInfo.getVersion());
		workspace.processItem(key, remoteInfo);
		fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
	}

	
	@Test
	public void remoteChangeNoConflict() throws SaveConflictException, BadPathException {
		// sync up new workspace
		syncNewWorkspace();
		
		// change remote and sync
		RemoteItemInfo remoteInfo = new RemoteItemInfo("b","mynewdata");
		workspace.processItem("/a/b", remoteInfo);
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
	}

	@Test
	public void localChangeNoConflict() throws SaveConflictException, BadPathException {
		// sync up new workspace
		syncNewWorkspace();
		
		String key = "/a/b";
		
		// remote info is not changed
		RemoteItemInfo remoteInfo = new RemoteItemInfo("b",fileSystem.get(key));
		// change local
		fileSystem.put(key, "mynewdata");
		
		// sync
		LocalItemInfo localInfo = workspace.processItem("/a/b", remoteInfo);
		Assert.assertNotNull("check local info returned", localInfo);
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
		
		Assert.assertEquals("check that local info is what we expect it to be", "mynewdata", localInfo.getVersion());
		
		// after getting local info, we will want to update the remote info and then re-process with the new remote info
		remoteInfo = new RemoteItemInfo(localInfo.getName(), localInfo.getVersion());
		workspace.processItem(key, remoteInfo);
		fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
		
		Assert.assertEquals("check that file system has not changed", "mynewdata", fileSystem.get(key));

	}

	@Test
	public void simultaneousChangeNoConflict() throws SaveConflictException, BadPathException {
		// sync up new workspace
		syncNewWorkspace();
		
		String key = "/a/b";
		
		// remote info is changed
		RemoteItemInfo remoteInfo = new RemoteItemInfo("b","mynewdata");
		// change local to the same thing
		fileSystem.put(key, "mynewdata");
		
		// sync
		LocalItemInfo localInfo = workspace.processItem(key, remoteInfo);
		Assert.assertNull("check no local info returned", localInfo);
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
				
		Assert.assertEquals("check that file system has not changed", "mynewdata", fileSystem.get(key));
	}
	
	@Test
	public void simultaneousChangeWithConflict() throws SaveConflictException, BadPathException {
		// sync up new workspace
		syncNewWorkspace();
		
		String key = "/a/b";
		
		// remote info is changed
		RemoteItemInfo remoteInfo = new RemoteItemInfo("b","mynewdata");
		// change local to the something different
		fileSystem.put(key, "myothernewdata");
		
		// sync
		try {
			LocalItemInfo localInfo = workspace.processItem(key, remoteInfo);
			fail("Did not have save conflict");
		} catch (SaveConflictException e) {
			// in a conflict, we have to resolve the situation and the simplest is to reject the local copy
			workspace.rejectLocalItem(key);
			workspace.processItem(key, remoteInfo); // do again
		}
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals(2, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertTrue(fileSet.contains("/a/b"));
				
		Assert.assertEquals("check that file system has changed", "mynewdata", fileSystem.get(key));
	}
	
	@Test
	public void remoteDeleteNoConflict() throws SaveConflictException, BadPathException {
		// sync up new workspace
		syncNewWorkspace();
		
		// change remote and sync
		workspace.processItem("/a/b", null); // no remote means a delete
		Set<String> fileSet = workspace.getFileSet();
		Assert.assertEquals("check one item deleted", 1, fileSet.size());
		Assert.assertTrue(fileSet.contains("/a"));
		Assert.assertFalse("check item deleted", fileSet.contains("/a/b"));
	}

	@Test
	public void remoteDeleteWithConflict() throws SaveConflictException, BadPathException {
		// sync up new workspace
		syncNewWorkspace();
		
		// remote delete when local change exists should result in conflict
		fail("Not complete");
	}

}

package com.picostuff.lockstep;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.picostuff.lockstep.exception.AddDuplicateException;
import com.picostuff.lockstep.exception.BadPathException;
import com.picostuff.lockstep.exception.MissingNodeException;

/**
 * Test to drive a shared memory interface
 * 
 * @author chenglim
 *
 */
public class SharedMemoryTest {
	private SharedMemory memory;
	
	@Before
	public void setup() {
		memory = new SharedMemory();
	}
	
	@After
	public void teardown() {
		memory = null;
	}
	
	@Test
	public void addNode() throws Exception {
		memory.addRootDirNode("a");
		memory.addNode("/a","b","1");
		// traverse tree and assert correct values
		Set<String> childrenNames = memory.getChildrenNames("/");
		Assert.assertEquals(1, childrenNames.size());
		Assert.assertEquals("a",new ArrayList<String>(childrenNames).get(0));
		childrenNames = memory.getChildrenNames("/a");
		Assert.assertEquals(1, childrenNames.size());
		Assert.assertEquals("b",new ArrayList<String>(childrenNames).get(0));
		childrenNames = memory.getChildrenNames("/a/b");
		Assert.assertEquals(0, childrenNames.size());
	}

	@Test
	public void addNodeBadPath() throws Exception {
		try {
			memory.addNode("", "a","1");
		} catch (BadPathException e) {
			// ok
			return;
		}
		fail("Did not throw BadPathException");
	}

	@Test
	public void addNodeMissing() throws Exception {
		try {
			memory.addNode("/a", "b","1");
		} catch (MissingNodeException e) {
			// ok
			return;
		}
		fail("Did not throw MissingNodeException");
	}

	@Test
	public void addNodeDuplicate() throws Exception {
		try {
			memory.addNode("/", "a","1");
			memory.addNode("/", "a","1");
		} catch (AddDuplicateException e) {
			// ok
			return;
		}
		fail("Did not throw AddDuplicateException");
	}

	@Test
	public void readNode() throws Exception {
		// TODO: add more properties as we build out the system
		memory.addRootDirNode("a");
		memory.addNode("/a","b","TestVersion");
		MemoryNode node = memory.copyNode("/a");
		Assert.assertEquals("a",node.getName());
		node = memory.copyNode("/a/b");
		Assert.assertEquals("b",node.getName());
		Assert.assertEquals("TestVersion", node.getVersion());
	}

	@Test
	public void changeNode() throws Exception {
		memory.addRootDirNode("a");
		memory.addNode("/a","b","TestVersion");
		
		MemoryNode node = memory.copyNode("/a/b");
		Assert.assertEquals("b",node.getName());
		Assert.assertEquals("TestVersion", node.getVersion());
		
		MemoryNode newNode = node.makeCopy();
		newNode.setVersion("NewTestVersion");
		memory.changeNode("/a/b", node, newNode);
		node = memory.copyNode("/a/b");
		Assert.assertEquals("b",node.getName());
		Assert.assertEquals("NewTestVersion", node.getVersion());
	}

	@Test
	public void removeNode() throws Exception {
		memory.addRootDirNode("a");
		memory.addNode("/a","b","1");
		MemoryNode node = memory.copyNode("/a/b");
		memory.removeNode("/a/b",node);
		
		// traverse tree and assert correct values
		Set<String> childrenNames = memory.getChildrenNames("/");
		Assert.assertEquals(1, childrenNames.size());
		Assert.assertEquals("a",new ArrayList<String>(childrenNames).get(0));
		childrenNames = memory.getChildrenNames("/a");
		Assert.assertEquals(0, childrenNames.size());
	}

}

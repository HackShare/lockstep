package com.picostuff.lockstep;

import java.util.Set;

import com.picostuff.lockstep.exception.AddDuplicateException;
import com.picostuff.lockstep.exception.BadPathException;
import com.picostuff.lockstep.exception.MissingNodeException;
import com.picostuff.lockstep.exception.SaveConflictException;

/**
 * Eventually, this will be an interface fronting different
 * implementations of a shared memory in a distributed system,
 * such as ZooKeeper, FireBase, or even a plain old database.
 * This class will help to flush out the requirements of
 * a shared memory.
 * 
 * @author chenglim
 *
 */
public class SharedMemory {
	static public final String DIR_NODE_VERSION = "dir";
	
	private MemoryNode rootNode;
	
	public SharedMemory() {
		rootNode = new MemoryNode("", DIR_NODE_VERSION);
	}

	public void addRootDirNode(String name) throws BadPathException, MissingNodeException, AddDuplicateException {
		addDirNode("/",name);
	}

	public void addDirNode(String path, String name) throws BadPathException, MissingNodeException, AddDuplicateException {
		addNode(path, name, DIR_NODE_VERSION);
	}

	public void addRootNode(String name, String version) throws BadPathException, MissingNodeException, AddDuplicateException {
		addNode("/",name,version);
	}
	
	public void addNode(String path, String name, String version) throws BadPathException, MissingNodeException, AddDuplicateException {
		findNode(path).addChild(name, version);
	}
	
	public Set<String> getChildrenNames(String path) throws BadPathException, MissingNodeException {
		return findNode(path).getChildrenNames();
	}
	
	public MemoryNode copyNode(String path) throws BadPathException, MissingNodeException {
		MemoryNode currentNode = findNode(path);
		return currentNode.makeCopy(); // return copy to shield internal storage from change (this simulates a client/server boundary)
	}
	
	public void changeNode(String path, MemoryNode oldNode, MemoryNode newNode) throws BadPathException, MissingNodeException, SaveConflictException {
		MemoryNode currentNode = findNode(path);
		// check that the version hasn't changed (this will come into play when we go multi-thread/process
		if (currentNode.getVersion().equals(oldNode.getVersion()) 
				&& currentNode.getName().equals(oldNode.getName())
				&& oldNode.getName().equals(newNode.getName())) {
			currentNode.updateWithCopy(newNode); // this simulates a client/server boundary
		} else {
			throw new SaveConflictException();
		}
	}
	
	public void removeNode(String path, MemoryNode oldNode) throws BadPathException, MissingNodeException, SaveConflictException {
		MemoryNode currentNode = findNode(path);
		// check that the version hasn't changed (this will come into play when we go multi-thread/process
		if (currentNode.getVersion().equals(oldNode.getVersion()) 
				&& currentNode.getName().equals(oldNode.getName())) {
			currentNode.removeFromParent();
		} else {
			throw new SaveConflictException();
		}
	}
	
	private MemoryNode findNode(String path) throws BadPathException, MissingNodeException {
		MemoryNode currentNode = rootNode;
		String[] parts = path.split("/",-1);
		if ((parts.length < 2) || !parts[0].equals(""))
			throw new BadPathException(path);
		for (String part:parts) {
			if (!part.equals("")) {
				currentNode = currentNode.getChild(part);
			}
		}
		return currentNode;
	}

}

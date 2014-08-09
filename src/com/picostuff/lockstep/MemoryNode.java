package com.picostuff.lockstep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.picostuff.lockstep.exception.AddDuplicateException;
import com.picostuff.lockstep.exception.MissingNodeException;

/**
 * A node in memory that has properties and can contain other nodes
 * 
 * @author chenglim
 *
 */
public class MemoryNode {
	private MemoryNode parent;
	private String name;
	private String version;
	private Map<String,MemoryNode> children;
	
	public MemoryNode(String name, String version) {
		this.name = name;
		this.version = version;
		children = new HashMap<String, MemoryNode>();
	}
	
	protected void addedToParent(MemoryNode parent) {
		this.parent = parent;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public boolean hasChild(String name) {
		return children.containsKey(name);
	}
	
	public MemoryNode addChild(String name, String version) throws AddDuplicateException {
		if (hasChild(name)) {
			throw new AddDuplicateException();
		}
		MemoryNode node = new MemoryNode(name, version);
		children.put(name, node);
		node.addedToParent(this);
		return node;
	}
	
	public void removeChild(String name) {
		children.remove(name);
	}
	
	public MemoryNode getChild(String name) throws MissingNodeException {
		MemoryNode node = children.get(name);
		if (node == null)
			throw new MissingNodeException();
		return node;
	}
	
	public Set<String> getChildrenNames() {
		return children.keySet();
	}
	
	public MemoryNode makeCopy() {
		MemoryNode copy = new MemoryNode(name, version);
		// TODO: update other properties later
		return copy;
	}
	
	public void updateWithCopy(MemoryNode copy) {
		setVersion(copy.getVersion());
		// TODO: update with other properties later
	}

	public void removeFromParent() {
		if (parent != null) {
			parent.removeChild(name);
		}
	}
}

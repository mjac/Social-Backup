package com.mjac.socialbackup.gui;

import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

public class BackupTreeModel extends DefaultTreeModel {
	private boolean showAncestors;

	private Vector treeModelListeners = new Vector();

	private BackupTreeItem rootPerson;

	public BackupTreeModel(BackupTreeItem root) {
		super(root);
		rootPerson = root;
	}

	/**
	 * Used to toggle between show ancestors/show descendant and to change the
	 * root of the tree.
	 */
	public void showAncestor(boolean b, Object newRoot) {
		showAncestors = b;
		BackupTreeItem oldRoot = rootPerson;
		if (newRoot != null) {
			rootPerson = (BackupTreeItem) newRoot;
		}
		fireTreeStructureChanged(oldRoot);
	}

	// ////////////// Fire events //////////////////////////////////////////////

	/**
	 * The only event raised by this model is TreeStructureChanged with the root
	 * as path, i.e. the whole tree has changed.
	 */
	protected void fireTreeStructureChanged(BackupTreeItem oldRoot) {
		int len = treeModelListeners.size();
		TreeModelEvent e = new TreeModelEvent(this, new Object[] { oldRoot });
		for (int i = 0; i < len; i++) {
			((TreeModelListener) treeModelListeners.elementAt(i))
					.treeStructureChanged(e);
		}
	}

	// ////////////// TreeModel interface implementation ///////////////////////

	/**
	 * Adds a listener for the TreeModelEvent posted after the tree changes.
	 */
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.addElement(l);
	}

	/**
	 * Returns the child of parent at index index in the parent's child array.
	 */
	public Object getChild(Object parent, int index) {
		BackupTreeItem p = (BackupTreeItem) parent;
		if (showAncestors) {
			if ((index > 0) && (p.getFather() != null)) {
				return p.getMother();
			}
			return p.getFather();
		}
		return p.getChildAt(index);
	}

	/**
	 * Returns the number of children of parent.
	 */
	public int getChildCount(Object parent) {
		BackupTreeItem p = (BackupTreeItem) parent;
		if (showAncestors) {
			int count = 0;
			if (p.getFather() != null) {
				count++;
			}
			if (p.getMother() != null) {
				count++;
			}
			return count;
		}
		return p.getChildCount();
	}

	/**
	 * Returns the index of child in parent.
	 */
	public int getIndexOfChild(Object parent, Object child) {
		BackupTreeItem p = (BackupTreeItem) parent;
		if (showAncestors) {
			int count = 0;
			BackupTreeItem father = p.getFather();
			if (father != null) {
				count++;
				if (father == child) {
					return 0;
				}
			}
			if (p.getMother() != child) {
				return count;
			}
			return -1;
		}
		return p.getIndexOfChild((BackupTreeItem) child);
	}

	/**
	 * Returns the root of the tree.
	 */
	public Object getRoot() {
		return rootPerson;
	}

	/**
	 * Returns true if node is a leaf.
	 */
	public boolean isLeaf(Object node) {
		BackupTreeItem p = (BackupTreeItem) node;
		if (showAncestors) {
			return ((p.getFather() == null) && (p.getMother() == null));
		}
		return p.getChildCount() == 0;
	}

	/**
	 * Removes a listener previously added with addTreeModelListener().
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.removeElement(l);
	}

	/**
	 * Messaged when the user has altered the value for the item identified by
	 * path to newValue. Not used by this model.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("*** valueForPathChanged : " + path + " --> "
				+ newValue);
	}
}
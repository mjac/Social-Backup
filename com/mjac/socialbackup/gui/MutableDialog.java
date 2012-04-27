package com.mjac.socialbackup.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public abstract class MutableDialog extends JDialog implements
		ChangeListener {
	public MutableDialog() {
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Frame owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Dialog owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Window owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Frame owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Dialog owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Dialog owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Window owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Window owner, String title,
			ModalityType modalityType) {
		super(owner, title, modalityType);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Frame owner, String title, boolean modal,
			GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Dialog owner, String title, boolean modal,
			GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public MutableDialog(Window owner, String title,
			ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		// TODO Auto-generated constructor stub
	}
	

	abstract protected void createGui();
	abstract public void updateGui();
	abstract public boolean holdsObject(Object obj);
	abstract public Object getHeldObject();
}

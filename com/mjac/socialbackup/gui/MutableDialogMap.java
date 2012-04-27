package com.mjac.socialbackup.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class MutableDialogMap extends HashMap<Object, MutableDialog> implements
		ChangeListener {
	public boolean focus(Object obj) {
		MutableDialog objDialog = get(obj);
		if (objDialog != null) {
			objDialog.toFront();
			return true;
		}

		return false;
	}

	public void autoRemove(MutableDialog cod) {
		final MutableDialog codRef = cod;
		codRef.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				remove(codRef.getHeldObject());
			}
		});
	}

	public void add(MutableDialog peerDialog) {
		put(peerDialog.getHeldObject(), peerDialog);
		autoRemove(peerDialog);
		peerDialog.setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		MutableDialog peerObj = get(ce.getSource());
		if (peerObj != null) {
			peerObj.stateChanged(ce);
		}
	}
}

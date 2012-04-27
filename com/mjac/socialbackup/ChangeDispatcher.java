package com.mjac.socialbackup;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ChangeDispatcher extends ArrayList<ChangeListener> implements
		ChangeListener {
	static Logger logger = Logger.getLogger(ChangeDispatcher.class);

	LinkedBlockingQueue<ChangeEvent> stateChanges = new LinkedBlockingQueue<ChangeEvent>();

	Thread stateDispatcherThread;
	boolean stateDispatching = false;

	public void createThread() {
		stateDispatching = true;
		stateDispatcherThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (stateDispatching) {
						ChangeEvent ce = stateChanges.take();
						dispatchStateChange(ce);
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		stateDispatcherThread.start();
	}

	public void dispatchStateChange(ChangeEvent ce) {
		for (ChangeListener cl : this) {
			cl.stateChanged(ce);
		}
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		try {
			// logger.trace(ce.getSource().getClass());
			stateChanges.put(ce);
		} catch (InterruptedException e) {
			logger.warn("State change dropped", e);
			return;
		}
	}
}

package com.mjac.socialbackup.services;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.mjac.socialbackup.state.LocalPeer;
import com.mjac.socialbackup.state.PeerTracker;
import com.mjac.socialbackup.state.RemotePeer;

public class Maintenance extends Thread {
	private static final Logger logger = Logger.getLogger(Maintenance.class);

	private static final Duration taskIntervalUnit = new Duration(15 * 1000);
	private static final long strategyInterval = 2;
	private static final long syncInterval = 1;
	private static final long timeoutInterval = 1;

	private static final Duration timeoutPeriod = new Duration(2 * 60 * 1000);

	private boolean running = true;

	protected LocalPeer user;

	public Maintenance(LocalPeer user) {
		this.user = user;
	}

	@Override
	public void run() {
		int taskIdx = 0;
		while (running) {
			//logger.trace("Running " + taskIdx);

			if (taskIdx % strategyInterval == 0) {
				user.createPlacingStrategy();
			}

			if (taskIdx % syncInterval == 0) {
				user.performSync();
			}

			if (taskIdx % timeoutInterval == 0) {
				timeoutConnections();
			}

			try {
				synchronized (this) {
					wait(taskIntervalUnit.getMillis());
				}
			} catch (InterruptedException e) {
				return;
			}

			++taskIdx;
		}
	}

	public void timeoutConnections() {
		DateTime cutoffDate = new DateTime().minus(timeoutPeriod);
		for (SslConnection sslConn : user.getConnections()) {
			if (!isConnectionActive(sslConn, cutoffDate)) {
				logger.trace("Disconnecting " + sslConn);
				sslConn.disconnect();
			}
		}
	}

	private boolean isConnectionActive(SslConnection sslConn,
			DateTime cutoffDate) {
		if (sslConn.getLastSent().isAfter(cutoffDate)
				|| sslConn.getLastReceived().isAfter(cutoffDate)) {
			return true;
		}

		RemotePeer peer = sslConn.getPeer();
		if (peer != null) {
			PeerTracker tracker = peer.getTracker();
			if (tracker.getLastSent().isAfter(cutoffDate)
					|| tracker.getLastReceived().isAfter(cutoffDate)) {
				return true;
			}

			// Make sure there is nothing in the message queue..!
		}

		return false;
	}

	public void exit() {
		running = false;
		synchronized (this) {
			interrupt();
		}
	}
}

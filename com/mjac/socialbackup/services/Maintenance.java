package com.mjac.socialbackup.services;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.state.Backup;
import com.mjac.socialbackup.state.BackupStrategy;
import com.mjac.socialbackup.state.Chunk;
import com.mjac.socialbackup.state.ChunkList;
import com.mjac.socialbackup.state.RemotePeer;
import com.mjac.socialbackup.state.PeerTracker;
import com.mjac.socialbackup.state.LocalPeer;
import com.mjac.socialbackup.state.BackupStrategy.ChunkComparer;

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
				createPlacingStrategy();
			}

			if (taskIdx % syncInterval == 0) {
				performSync();
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

	/** Create a strategy for placing available chunks onto peers. */
	public void createPlacingStrategy() {
		PriorityQueue<ChunkComparer> comparer = new PriorityQueue<ChunkComparer>();

		for (Backup backup : user.getBackups()) {
			BackupStrategy backupStrategy = backup.getStrategy();
			if (backupStrategy == null) {
				backupStrategy = user.getBackupStrategy();
			}
			backupStrategy.place(backup, user, comparer);
		}

		logger.trace(comparer.toString());

		// Collection<RemotePeer> remotePeers = user.getPeers();
		HashMap<RemotePeer, ChunkList> newChunkLists = new HashMap<RemotePeer, ChunkList>();

		for (ChunkComparer cc : comparer) {
			if (cc.getSuitability() <= 0.0) {
				continue;
			}

			RemotePeer remotePeer = cc.getPeer();
			ChunkList chunkList = newChunkLists.get(remotePeer);
			if (chunkList == null) {
				chunkList = new ChunkList();
				newChunkLists.put(remotePeer, chunkList);
			}

			Chunk chunk = cc.getChunk();
			if (chunkList.canHave(remotePeer, chunk)) {
				chunkList.set(cc.getChunk());
			}
		}

		for (Entry<RemotePeer, ChunkList> entry : newChunkLists.entrySet()) {
			entry.getKey().setChunkList(entry.getValue());
			logger.trace(entry.getKey().getAlias() + " now contains: "
					+ entry.getValue());
		}
	}

	/** Check to see if peers have messages to send or needs sync. */
	public void performSync() {
		for (RemotePeer peer : user.getPeers()) {
			peer.maintenance();
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

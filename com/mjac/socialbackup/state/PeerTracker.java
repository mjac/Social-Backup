package com.mjac.socialbackup.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.joda.time.DateTime;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.msg.ChunkMessage;

public class PeerTracker implements Serializable {
	private static final long serialVersionUID = 1L;

	// WATCH OUT THESE CAN BE NULL:
	protected DateTime lastSyncSent;
	protected DateTime lastSyncReceived;
	protected DateTime lastReceived;
	protected DateTime lastSent;

	protected Id challengeChunkId;
	protected int challengeAmount = 0;
	protected int challengeWins = 0;

	protected int connectAmount = 0;
	protected int connectSuccess = 0;

	public void syncSent() {
		lastSyncSent = new DateTime();
	}

	public DateTime getSyncSent() {
		return lastSyncSent;
	}

	public void syncReceived() {
		lastSyncReceived = new DateTime();
	}

	public DateTime getSyncReceived() {
		return lastSyncReceived;
	}

	public void messageReceived() {
		lastReceived = new DateTime();
	}

	public void messageSent() {
		lastSent = new DateTime();
	}

	public DateTime getLastSent() {
		return lastSent;
	}

	public DateTime getLastReceived() {
		return lastReceived;
	}

	public void setChallenge(Id chunkId) {
		challengeChunkId = chunkId;
	}

	/** Test the user by requesting a random chunk they should have. */
	public Id createChallenge(RemotePeer peer) {
		Id[] chunkIds = peer.getChunkList().getIds().toArray(new Id[] {});
		Random rng = new Random();
		int rngInt = rng.nextInt();
		challengeChunkId = chunkIds[rngInt % chunkIds.length];
		return challengeChunkId;
	}

	public void checkChallenge(ChunkMessage chunkContent,
			RemotePeer peer, LocalPeer user) {
		try {
			if (challengeChunkId == null
					|| !chunkContent.getChunk().getId()
							.equals(challengeChunkId)) {
				return;
			}
		} catch (NullPointerException npe) {
			return;
		}

		Chunk diskChunk = user.getChunkList().get(challengeChunkId);
		if (diskChunk == null) {
			resetChallenge();
			return;
		}

		byte[] localData;
		try {
			localData = diskChunk.getEncryptedData(peer);
		} catch (IOException e) {
			resetChallenge();
			return;
		}

		++challengeAmount;
		if (localData.equals(chunkContent.data)) {
			++challengeWins;
		}
	}

	public void resetChallenge() {
		challengeChunkId = null;
	}

	public void connectAttempt(boolean connected) {
		++connectAmount;
		if (connected) {
			++connectSuccess;
		}
	}
}

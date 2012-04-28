package com.mjac.socialbackup.state;

import java.io.Serializable;
import java.util.PriorityQueue;

import com.mjac.socialbackup.Id;

public class BackupStrategy implements Serializable {
	public class ChunkComparer implements Comparable<ChunkComparer> {
		protected RemotePeer peer;
		protected Chunk chunk;
		protected double suitability;

		public ChunkComparer(RemotePeer peer, Chunk chunk, double suitability) {
			this.peer = peer;
			this.chunk = chunk;
			this.suitability = suitability;
		}

		public RemotePeer getPeer() {
			return peer;
		}

		public Chunk getChunk() {
			return chunk;
		}

		public double getSuitability() {
			return suitability;
		}

		/** Inverse standard double ordering (smallest last). Produce total ordering. */
		@Override
		public int compareTo(ChunkComparer otherChunk) {
			int firstCompare = new Double(otherChunk.suitability)
					.compareTo(suitability);
			if (firstCompare == 0) {
				return new Integer(otherChunk.getChunk().getId().hashCode())
						.compareTo(getChunk().getId().hashCode());
			}
			return firstCompare;
		}

		@Override
		public String toString() {
			return suitability + " = " + chunk.getId() + " -> "
					+ peer.getAlias();
		}
	}

	/** Create a priority queue. */
	public void place(Backup backup, LocalPeer user,
			PriorityQueue<ChunkComparer> comparer) {
		ChunkList localStore = user.getChunkList();
		for (Id chunkId : backup.getChunkIds()) {
			Chunk chunk = localStore.get(chunkId);
			if (chunk == null) {
				continue;
			}

			for (RemotePeer peer : user.getPeers()) {
				Double suitability = suitability(peer, user, backup, chunk);
				comparer.add(new ChunkComparer(peer, chunk, suitability));
			}
		}
	}

	/**
	 * Determine the suitability of peer for storing a chunk, associated with a
	 * backup, owned by a user.
	 */
	public double suitability(RemotePeer peer, LocalPeer user, Backup backup,
			Chunk chunk) {
		return suitability(peer, user, backup) * suitability(peer, user, chunk);
	}

	/** Determine the suitability of peer for storing a backup owned by a user. */
	public double suitability(RemotePeer peer, LocalPeer user, Backup backup) {
		return 1.0;
	}

	/** Determine the suitability of peer for storing a chunk owned by a user. */
	public double suitability(RemotePeer peer, LocalPeer user, Chunk chunk) {
		// Determining if peer has chunk is done later
		// if (peer.hasChunk(chunk.getId())) {
		// return 0.0;
		// }

		return 1.0;
	}
}

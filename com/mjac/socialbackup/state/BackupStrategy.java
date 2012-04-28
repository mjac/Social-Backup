package com.mjac.socialbackup.state;

import java.io.Serializable;
import java.util.PriorityQueue;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.actors.LocalUser;
import com.mjac.socialbackup.actors.RemoteUser;

public class BackupStrategy implements Serializable {
	public class ChunkComparer implements Comparable<ChunkComparer> {
		protected RemoteUser peer;
		protected Chunk chunk;
		protected double suitability;

		public ChunkComparer(RemoteUser peer, Chunk chunk, double suitability) {
			this.peer = peer;
			this.chunk = chunk;
			this.suitability = suitability;
		}

		public RemoteUser getPeer() {
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
	public void place(Backup backup, LocalUser user,
			PriorityQueue<ChunkComparer> comparer) {
		ChunkList localStore = user.getChunkList();
		for (Id chunkId : backup.getChunkIds()) {
			Chunk chunk = localStore.get(chunkId);
			if (chunk == null) {
				continue;
			}

			for (RemoteUser peer : user.getPeers()) {
				Double suitability = suitability(peer, user, backup, chunk);
				comparer.add(new ChunkComparer(peer, chunk, suitability));
			}
		}
	}

	/**
	 * Determine the suitability of peer for storing a chunk, associated with a
	 * backup, owned by a user.
	 */
	public double suitability(RemoteUser peer, LocalUser user, Backup backup,
			Chunk chunk) {
		return suitability(peer, user, backup) * suitability(peer, user, chunk);
	}

	/** Determine the suitability of peer for storing a backup owned by a user. */
	public double suitability(RemoteUser peer, LocalUser user, Backup backup) {
		return 1.0;
	}

	/** Determine the suitability of peer for storing a chunk owned by a user. */
	public double suitability(RemoteUser peer, LocalUser user, Chunk chunk) {
		// Determining if peer has chunk is done later
		// if (peer.hasChunk(chunk.getId())) {
		// return 0.0;
		// }

		return 1.0;
	}
}

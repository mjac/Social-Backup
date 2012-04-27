package com.mjac.socialbackup.msg;

import com.mjac.socialbackup.state.Chunk;
import com.mjac.socialbackup.state.Peer;

/** Return chunk owned by remote peer. */
public class ChunkReturnMessage extends ChunkMessage {
	public ChunkReturnMessage(Chunk chunk, Peer peer) {
		super(chunk, peer);
	}
}

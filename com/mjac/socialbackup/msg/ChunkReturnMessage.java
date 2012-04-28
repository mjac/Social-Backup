package com.mjac.socialbackup.msg;

import com.mjac.socialbackup.actors.User;
import com.mjac.socialbackup.state.Chunk;

/** Return chunk owned by remote peer. */
public class ChunkReturnMessage extends ChunkMessage {
	public ChunkReturnMessage(Chunk chunk, User peer) {
		super(chunk, peer);
	}
}

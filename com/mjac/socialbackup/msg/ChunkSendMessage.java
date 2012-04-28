package com.mjac.socialbackup.msg;

import com.mjac.socialbackup.actors.User;
import com.mjac.socialbackup.state.Chunk;

/** Set chunk on peer */
public class ChunkSendMessage extends ChunkMessage {
	public ChunkSendMessage(Chunk chunk, User peer) {
		super(chunk, peer);
	}
}

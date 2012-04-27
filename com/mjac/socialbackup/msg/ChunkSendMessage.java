package com.mjac.socialbackup.msg;

import com.mjac.socialbackup.state.Chunk;
import com.mjac.socialbackup.state.Peer;

/** Set chunk on remote peer. */
public class ChunkSendMessage extends ChunkMessage {
	public ChunkSendMessage(Chunk chunk, Peer peer) {
		super(chunk, peer);
	}
}

package com.mjac.socialbackup.msg;

import com.mjac.socialbackup.state.ChunkList;

public class ChunkListMessage extends PeerMessage {
	private static final long serialVersionUID = 1L;

	/** Chunks owned by the sender that the sender thinks the receiver has. */
	protected ChunkList senderChunks;

	/** Chunks owned by the receiver that the sender says they have. */
	protected ChunkList receiverChunks;

	public ChunkListMessage(ChunkList senderChunks, ChunkList receiverChunks) {
		this.receiverChunks = receiverChunks;
		this.senderChunks = senderChunks;
	}

	public ChunkList getReceiverChunks() {
		return receiverChunks;
	}

	public ChunkList getSenderChunks() {
		return senderChunks;
	}
}

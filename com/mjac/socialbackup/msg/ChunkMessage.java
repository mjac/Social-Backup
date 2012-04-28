package com.mjac.socialbackup.msg;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.mjac.socialbackup.Daemon;
import com.mjac.socialbackup.state.Chunk;
import com.mjac.socialbackup.state.LocalPeer;
import com.mjac.socialbackup.state.Peer;

/** A lazy chunk transfer device. */
abstract public class ChunkMessage extends PeerMessage {
	static Logger logger = Logger.getLogger(ChunkMessage.class);

	private static final long serialVersionUID = 1L;

	public Chunk chunk;
	public byte[] data;

	private transient Peer peer;

	public ChunkMessage(Chunk chunk, Peer peer) {
		this.chunk = chunk;
		this.peer = peer;
	}

	public Chunk getChunk() {
		return chunk;
	}

	public byte[] getData() {
		return data;
	}

	/**
	 * @todo Use a verifyiable interface on lots of objects! Need to check them
	 *       recursively then check them together.
	 */
	@Override
	public boolean valid() {
		return data != null && chunk != null && chunk.getId() != null
				&& chunk.getOutputSize() == data.length;
	}

	private void writeObject(ObjectOutputStream out, LocalPeer localPeer) throws IOException,
			ClassNotFoundException {
		data = chunk.getEncryptedData(peer, localPeer);
		logger.info("sent " + chunk.getOutputSize() + " " + new DateTime().getMillis());
		out.defaultWriteObject();
	}
}

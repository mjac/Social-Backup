package com.mjac.socialbackup.msg;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.mjac.socialbackup.actors.User;
import com.mjac.socialbackup.state.Chunk;

/** A lazy chunk transfer device. */
abstract public class ChunkMessage extends PeerMessage {
	static Logger logger = Logger.getLogger(ChunkMessage.class);

	private static final long serialVersionUID = 1L;

	public Chunk chunk;
	public byte[] data;

	private transient User user;

	public ChunkMessage(Chunk chunk, User user) {
		this.chunk = chunk;
		this.user = user;
	}

	public Chunk getChunk() {
		return chunk;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public boolean valid() {
		return data != null && chunk != null && chunk.getId() != null
				&& chunk.getOutputSize() == data.length;
	}

	private void writeObject(ObjectOutputStream out) throws IOException,
			ClassNotFoundException {
		data = chunk.getEncryptedData(user);
		logger.info("sent " + chunk.getOutputSize() + " " + new DateTime().getMillis());
		out.defaultWriteObject();
	}
}

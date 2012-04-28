package com.mjac.socialbackup.state;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.ReadableDuration;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.msg.ChunkListMessage;
import com.mjac.socialbackup.msg.ChunkMessage;
import com.mjac.socialbackup.msg.ChunkReturnMessage;
import com.mjac.socialbackup.msg.ChunkSendMessage;
import com.mjac.socialbackup.msg.Message;
import com.mjac.socialbackup.msg.StatusMessage;
import com.mjac.socialbackup.services.SslConnection;

public class RemotePeer extends Peer {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RemotePeer.class);

	private static final String fileExtension = ".remote";

	/** Connection that this peer is associated with. */
	transient protected SslConnection sslPeer;
	transient Thread senderThread;
	transient boolean sending = false;

	/** Messages to be sent to the client. */
	transient protected LinkedBlockingQueue<Message> outgoing = new LinkedBlockingQueue<Message>();

	/** List of chunks owned by the remote peer, stored locally. */
	protected ChunkList remoteChunks = new ChunkList();

	/** Allocation to the remote peer. */
	protected long remoteAllocation = 0L;

	public RemotePeer(Id id, File localStore) {
		super(id, localStore);
	}

	public void setRemoteAllocation(long remoteAllocation) {
		this.remoteAllocation = remoteAllocation;
	}

	public long getRemoteAllocation() {
		return remoteAllocation;
	}

	public long getRemoteFreeSpace() {
		return remoteAllocation - chunks.getSize();
	}

	public SslConnection getHandler() {
		return sslPeer;
	}

	public boolean isHandled() {
		return sslPeer != null;
	}

	public void removeHandler() {
		sslPeer = null;
	}

	public void setHandler(SslConnection sslPeer) {
		this.sslPeer = sslPeer;
		sslPeer.setUser(this);
	}

	public void startSender() {
		sending = true;

		if (senderThread != null && senderThread.isAlive()) {
			return;
		}

		senderThread = new Thread(new Runnable() {
			@Override
			public void run() {
				sender();
			}
		});

		senderThread.start();
	}

	public void sender() {
		logger.trace("Sending thread started for " + id);
		while (sending && sslPeer.isConnected()) {
			try {
				Message message = outgoing.take();
				if (!(sending && sslPeer.isConnected() && sslPeer.send(message))) {
					// Check to see if it has failed
					// and put back on the queue?
					send(message);
				}
			} catch (InterruptedException e) {
				logger.warn("Sending interrupted", e);
				sending = false;
			}
		}
		logger.trace("Sending thread quit for " + id);
	}

	public void stopSender() {
		sending = false;
	}

	public void send(Message message) {
		/*
		 * Message[] currentOutgoing = outgoing.toArray(new Message[]{}); if
		 * (ArrayUtils.contains(currentOutgoing, message)) { return; }
		 */
		outgoing.remove(message); // Remove any duplicate messages, update with
									// latest copy!

		logger.trace(getAlias() + " <- " + message.getClass().getSimpleName());

		tracker.messageSent();
		outgoing.add(message);
	}

	public void sendChunkListing() {
		tracker.syncSent();
		send(new ChunkListMessage(chunks, remoteChunks));
	}

	public void receive(Message message, LocalPeer localPeer) {
		logger.trace("@" + localPeer.getAlias() + " <- "
				+ message.getClass().getSimpleName() + " <- " + getAlias());

		tracker.messageReceived();

		if (!message.valid()) {
			logger.warn("Invalid message " + message);
			return;
		}

		if (message instanceof StatusMessage) {
			updateStatus((StatusMessage) message);
		} else if (message instanceof ChunkMessage) {
			receiveChunk((ChunkMessage) message, localPeer);
		} else if (message instanceof ChunkListMessage) {
			receiveLists((ChunkListMessage) message, localPeer);
		}
	}

	private void syncSenderList(ChunkList senderChunks) {
		ChunkList compare = senderChunks.missing(remoteChunks);
		for (Chunk chunk : compare.getChunks()) {
			send(new ChunkReturnMessage(chunk, this));
		}
	}

	private void syncReceiverList(ChunkList receiverChunks, LocalPeer localPeer) {
		ChunkList compare = receiverChunks.missing(chunks);
		for (Chunk chunk : compare.getChunks()) {
			send(new ChunkSendMessage(chunk, localPeer));
		}
	}
	
	private void receiveLists(ChunkListMessage message, LocalPeer localPeer) {
		tracker.syncReceived();
		syncReceiverList(message.getReceiverChunks(), localPeer);
		syncSenderList(message.getSenderChunks());
	}

	private boolean receiveChunk(ChunkMessage message, LocalPeer localPeer) {
		Chunk chunk = message.getChunk();
		byte[] data = message.getData();

		if (message instanceof ChunkSendMessage) {
			return receiveRemoteChunk(chunk, data);
		} else if (message instanceof ChunkReturnMessage) {
			return localPeer.receiveChunkData(chunk, data);
		} else {
			return false;
		}
	}

	/**
	 * @todo Should have some synchronization here
	 */
	private boolean receiveRemoteChunk(Chunk chunk, byte[] data) {
		if (remoteChunks.has(chunk)) {
			return false;
		}
		
		long freespace = getRemoteFreeSpace();
		if (freespace < data.length) {
			return false;
		}

		logger.info("receive " + new DateTime().getMillis());
		return writeChunkData(chunk, data, remoteChunks);
	}

	public boolean needsConnect() {
		return !(outgoing.isEmpty() || isHandled());
	}
	
	public boolean needsChunkListing(ReadableDuration listValidDuration) {
		return tracker.needsSync(listValidDuration);
	}

	public boolean needsStatus(ReadableDuration statusValidDuration) {
		return tracker.needsStatus(statusValidDuration);
	}

	@Override
	public File getFile() {
		return getFile(fileExtension);
	}

	static public Id fileId(File checkFile) {
		return fileId(checkFile, fileExtension);
	}

	// SYNC TO DISK

	@Override
	public RemotePeer restore() {
		Peer cp = super.restore();
		if (cp instanceof RemotePeer) {
			return (RemotePeer) cp;
		}
		return null;
	}
	
	private void readObject(java.io.ObjectInputStream stream)
    throws IOException, ClassNotFoundException
    {
		stream.defaultReadObject();
		outgoing = new LinkedBlockingQueue<Message>();
		sending = false;
    }
}

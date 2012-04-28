package com.mjac.socialbackup.state;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

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

	public RemotePeer(Id id) {
		super(id);
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
		ChunkListMessage message = new ChunkListMessage(chunks, remoteChunks);
		send(message);
	}

	public void sendStatus(LocalPeer localPeer) {
		StatusMessage message = new StatusMessage(localPeer.getAlias(),
				localPeer.getEmail(), localPeer.getHost(), localPeer.getPort(),
				localPeer.getAllocation());
		send(message);
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

	private void receiveLists(ChunkListMessage message, LocalPeer localPeer) {
		// if (tracker.getSyncSent())
		tracker.syncReceived();
		syncReceiverList(message.getReceiverChunks(), localPeer);
		syncSenderList(message.getSenderChunks());
		// message.getSenderChunks();
	}

	private void syncSenderList(ChunkList senderChunks) {
		ChunkList compare = senderChunks.missing(remoteChunks);

		if (compare.isEmpty()) {
			return;
		}

		for (Chunk chunk : compare.getChunks()) {
			ChunkReturnMessage crm = new ChunkReturnMessage(chunk, this);
			send(crm);
		}
	}

	private void syncReceiverList(ChunkList receiverChunks, LocalPeer localPeer) {
		ChunkList compare = receiverChunks.missing(chunks);

		if (compare.isEmpty()) {
			return;
		}

		for (Chunk chunk : compare.getChunks()) {
			ChunkSendMessage crm = new ChunkSendMessage(chunk, localPeer);
			send(crm);
		}
	}

	private boolean receiveChunk(ChunkMessage message, LocalPeer localPeer) {
		Chunk chunk = message.getChunk();
		byte[] data = message.getData();

		if (message instanceof ChunkSendMessage) {
			return receiveRemoteChunk(chunk, data, localPeer);
		} else if (message instanceof ChunkReturnMessage) {
			return receiveLocalChunk(chunk, data, localPeer);
		} else {
			return false;
		}
	}

	private boolean receiveLocalChunk(Chunk chunk, byte[] data, LocalPeer localPeer) {
		return localPeer.receiveChunkData(chunk, data);
	}

	/**
	 * Should have some synchronization here.
	 * 
	 * @param chunk
	 * @param data
	 * @return
	 */
	private boolean receiveRemoteChunk(Chunk chunk, byte[] data, LocalPeer localPeer) {
		if (remoteChunks.has(chunk)) {
			return false;
		}
		
		long freespace = getRemoteFreeSpace();
		if (freespace < data.length) {
			return false;
		}

		logger.info("receive " + new DateTime().getMillis());
		return writeChunkData(chunk, data, remoteChunks, localPeer);
	}

	/** Connect and send messages, if not already connected. */
	public void connectForSend(LocalPeer localPeer) {
		if (outgoing.isEmpty() || isHandled()) {
			return;
		}

		localPeer.connect(this);
	}

	/**
	 * Call when receive a chunk list too!
	 */
	public void maintenance(LocalPeer localPeer) {
		DateTime now = new DateTime();

		DateTime listValidAfter = now.minus(localPeer.getListDuration());
		if (tracker.getSyncReceived() == null || tracker.getSyncSent() == null
				|| tracker.getSyncReceived().isBefore(listValidAfter)
				|| tracker.getSyncSent().isBefore(listValidAfter)) {
			sendChunkListing();
		}

		DateTime lastStatusRequired = now.minus(localPeer.getStatusDuration());
		if (tracker.getLastReceived() == null || tracker.getLastSent() == null
				|| tracker.getLastReceived().isBefore(lastStatusRequired)
				|| tracker.getLastSent().isBefore(lastStatusRequired)) {
			sendStatus(localPeer);
		}

		if (!isConnectionUsed(localPeer)) {
			connectForSend(localPeer);
		}
	}

	/** Is there another peer with the same address, that is already handled? */
	public boolean isConnectionUsed(LocalPeer localPeer) {
		for (RemotePeer checkPeer : localPeer.getPeers()) {
			if (getHost().equals(checkPeer.getHost())
					&& getPort() == checkPeer.getPort()
					&& checkPeer.isHandled()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public File getFile(LocalPeer localPeer) {
		return getFile(localPeer, fileExtension);
	}

	static public Id fileId(File checkFile) {
		return fileId(checkFile, fileExtension);
	}

	// SYNC TO DISK

	@Override
	public RemotePeer restore(LocalPeer localPeer) {
		Peer cp = super.restore(localPeer);
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

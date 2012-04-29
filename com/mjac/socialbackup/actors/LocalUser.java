package com.mjac.socialbackup.actors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import javax.crypto.SecretKey;
import javax.net.ssl.SSLContext;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

import com.mjac.socialbackup.ChangeDispatcher;
import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.RandomisedId;
import com.mjac.socialbackup.crypto.BouncyCastleGenerator;
import com.mjac.socialbackup.crypto.KeystoreManager;
import com.mjac.socialbackup.crypto.OpenTrustManager;
import com.mjac.socialbackup.msg.ChunkListMessage;
import com.mjac.socialbackup.msg.ChunkMessage;
import com.mjac.socialbackup.msg.ChunkReturnMessage;
import com.mjac.socialbackup.msg.ChunkSendMessage;
import com.mjac.socialbackup.msg.Message;
import com.mjac.socialbackup.msg.StatusMessage;
import com.mjac.socialbackup.services.Maintenance;
import com.mjac.socialbackup.services.SslConnection;
import com.mjac.socialbackup.services.SslServer;
import com.mjac.socialbackup.state.Backup;
import com.mjac.socialbackup.state.BackupStrategy;
import com.mjac.socialbackup.state.Chunk;
import com.mjac.socialbackup.state.ChunkData;
import com.mjac.socialbackup.state.ChunkList;
import com.mjac.socialbackup.state.BackupStrategy.ChunkComparer;

/** Has a private key associated */
public class LocalUser extends User implements ChangeListener {
	static private final long serialVersionUID = 1L;

	static private final Logger logger = Logger.getLogger(LocalUser.class);

	static public final int defaultPort = 134;

	static protected final String fileExtension = ".local";

	/** Duration a chunk list remains valid. */
	static protected final Duration listDuration = new Duration(30 * 1000);

	/** Duration a user's status remains active. */
	static protected final Duration statusDuration = new Duration(15 * 1000);

	protected boolean recovering = false;

	/** List of files backed up by this user. */
	protected ArrayList<Backup> backups = new ArrayList<Backup>();

	protected BackupStrategy strategy = new BackupStrategy();

	/** Clients that we have information for. */
	transient protected Map<Id, RemoteUser> peers = new HashMap<Id, RemoteUser>();

	/** List of known clients. Only used for serialization. */
	protected ArrayList<Id> peerIds = new ArrayList<Id>();

	transient protected ArrayList<SslConnection> connections;

	transient protected KeystoreManager keystoreManager;

	transient protected File directory;

	transient protected SslServer server;
	transient protected Maintenance maintenance;

	transient protected ChangeDispatcher changeDispatcher;

	public LocalUser(Id id, File localStore) {
		super(id, localStore);

		// Service defaults!
		alias = "";
		email = "";
		host = "";
		port = defaultPort;
		allocation = 100 << 20;

		this.directory = localStore;

		connections = new ArrayList<SslConnection>();
	}

	public void setChangeDispatcher(ChangeDispatcher changeDispatcher) {
		this.changeDispatcher = changeDispatcher;
	}

	public File getDirectory() {
		return directory;
	}

	@Override
	public File getFile() {
		return getFile(fileExtension);
	}

	static public Id fileId(File checkFile) {
		return fileId(checkFile, fileExtension);
	}

	public BackupStrategy getBackupStrategy() {
		return strategy;
	}

	// KEY MANIPULATION METHODS.

	public void setKeystoreManager(KeystoreManager keystoreManager) {

		this.keystoreManager = keystoreManager;
	}

	public SecretKey getSecretKey() {
		try {
			return keystoreManager.getSecretKey(id.toString());
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public PublicKey getPublicKey() {
		throw new NotImplementedException();
	}

	public PrivateKey getPrivateKey() {
		throw new NotImplementedException();
	}

	public boolean hasKeys() {
		try {
			PrivateKey prvKey = keystoreManager.getPrivateKey(id.toString());
			SecretKey secKey = keystoreManager.getSecretKey(id.toString());
			return prvKey != null && secKey != null;
		} catch (GeneralSecurityException gse) {
			logger.warn("Error checking whether keys exist", gse);
			return false;
		}
	}

	public void createKeys() throws GeneralSecurityException, IOException {
		createKeyPair();
		createSecretKey();
		keystoreManager.store();
	}

	private void createKeyPair() throws GeneralSecurityException {
		KeyPair kp = BouncyCastleGenerator.rsaPair();
		X509Certificate xCert = BouncyCastleGenerator.generateCertificate(kp,
				email);
		keystoreManager.setKey(id.toString(), kp.getPrivate(),
				new Certificate[] { xCert });
	}

	private void createSecretKey() throws GeneralSecurityException {
		keystoreManager.setKey(id.toString(), BouncyCastleGenerator.aesKey());
	}

	public void updateCertificate() {
		logger.trace("Updating certificate for peer");
		keystoreManager.updateCert(id.toString(), email);
	}

	// BACKUP FILES

	public boolean addBackup(Backup newBackup) {
		if (!newBackup.write(this)) {
			return false;
		}

		backups.add(newBackup);
		persist();

		return true;
	}

	public ArrayList<Backup> getBackups() {
		return backups;
	}

	// PEER CONNECTIONS

	public Collection<RemoteUser> getPeers() {
		return peers.values();
	}

	public Collection<SslConnection> getConnections() {
		return connections;
	}

	public SSLContext getSslContext() {
		SSLContext sslcontext;
		try {
			sslcontext = SSLContext.getInstance("TLS");
			sslcontext
					.init(keystoreManager.factory().getKeyManagers(),
							OpenTrustManager.trustAllCertificates(),
							new SecureRandom());
			return sslcontext;
		} catch (Exception e) {
			logger.error("SSL context could not be created", e);
			return null;
		}
	}

	public RemoteUser createPeer(SslConnection sslPeer, long remoteAllocation) {
		PublicKey publicKey = sslPeer.getPublicKey();
		String existingAlias = keystoreManager.getRawId(publicKey);

		RemoteUser newPeer = null;

		if (existingAlias == null) {
			logger.trace("Key not assigned, adding to keystore");
			// Not associated in keystore, generate new ID.
			newPeer = new RemoteUser(new RandomisedId(), directory, sslPeer);
			newPeer.setRemoteAllocation(remoteAllocation);

			PublicKey existingKey = null;
			try {
				existingKey = keystoreManager.getPublicKey(newPeer.getId()
						.toString());
			} catch (GeneralSecurityException e) {
				logger.warn("Could not get public key", e);
			}

			if (existingKey != null) {
				logger.warn("Random alias already assigned to key - generated IDs may not be random!");
				return null;
			}

			try {
				keystoreManager.setKey(newPeer.getId().toString(),
						sslPeer.getPublicKey());
				keystoreManager.store();
				logger.trace("Key stored in keystore");
			} catch (Exception e) {
				logger.warn("Could not set public key for new peer", e);
				return null;
			}
		} else {
			// Associated in keystore, create peer.
			logger.trace("Key already assigned to " + existingAlias);
			newPeer = new RemoteUser(new Id(existingAlias), directory, sslPeer);
			// Does the peer exist though or is it just in the keystore?
		}

		RemoteUser alreadyAssigned = peers.get(newPeer.getId());
		if (alreadyAssigned != null) {
			logger.warn("ID already assigned to peer - already a peer associated");
			return null;
		}

		// Replaces any duplicates
		// http://stackoverflow.com/questions/1669885/java-hashmap-duplicates
		peers.put(newPeer.getId(), newPeer);

		persist();
		newPeer.persist();

		newPeer.setHandler(sslPeer);

		newPeer.startSender();
		sslPeer.startReceiver(this);

		changeDispatcher.stateChanged(new ChangeEvent(newPeer));
		changeDispatcher.stateChanged(new ChangeEvent(sslPeer));

		return newPeer;
	}

	public boolean handleConnection(SslConnection sslPeer) {
		logger.trace("Handling peer");

		sslPeer.handshake();

		PublicKey peerKey = sslPeer.getPublicKey();
		if (peerKey == null) {
			logger.warn("SSL connection missing public key, closing");
			sslPeer.disconnect();
			return false;
		}

		String peerIdStr = keystoreManager.getRawId(peerKey);
		if (peerIdStr != null) {
			Id peerId = new Id(peerIdStr);
			logger.trace("Found peer key " + peerId);

			// handle
			if (peerId.equals(id)) {
				logger.warn("Cannot connect to yourself (id " + peerId + ")");
				sslPeer.disconnect();
				return false;
			}

			RemoteUser newPeer = peers.get(peerId);

			if (newPeer == null) {
				logger.warn("Peer missing for recognised key, treating as SSL peer");
			} else {
				logger.trace("Key corresponds to existing peer "
						+ newPeer.getAlias());

				newPeer.setHandler(sslPeer);

				newPeer.startSender();
				sslPeer.startReceiver(this);

				newPeer.send(new StatusMessage(this));

				changeDispatcher.stateChanged(new ChangeEvent(newPeer));
				changeDispatcher.stateChanged(new ChangeEvent(sslPeer));

				return true;
			}
		}

		logger.trace("Unrecognised peer, handling and putting into friend request queue");
		sslPeer.startReceiver(this);
		sslPeer.sendStatus(this);

		connections.add(sslPeer);

		changeDispatcher.stateChanged(new ChangeEvent(sslPeer));

		return true;
	}

	// SERVICE IS COMPOSED OF SERVER & MAINTENANCE

	public boolean startService() {
		startMaintenance();
		return startServer();
	}

	public void stopService() {
		stopServer();
		stopMaintenance();
	}

	// SERVER CONTROLS.

	public boolean startServer() {
		logger.info("Starting service");
		try {
			server = new SslServer(this, changeDispatcher);
			server.start();
		} catch (Exception e) {
			logger.error("Could not start peer handling service", e);
			return false;
		}
		return true;
	}

	public void placePeer(final SslConnection newPeer) {
		// Called by the server thread so do not want to block here.
		new Thread() {
			@Override
			public void run() {
				handleConnection(newPeer);
			}
		}.start();
	}

	public void stopServer() {
		if (server != null) {
			server.close();
		}
	}

	// MAINTENANCE SERVICE

	public void startMaintenance() {
		logger.info("Starting maintenance");
		try {
			maintenance = new Maintenance(this);
			maintenance.start();
		} catch (Exception e) {
			logger.error("Could not start maintenance", e);
		}
	}

	public void stopMaintenance() {
		if (maintenance != null) {
			maintenance.exit();
		}
	}

	// SYNCHRONISATION TO DISK

	@Override
	public LocalUser restore() throws IOException, ClassNotFoundException {
		User cp = super.restore();

		if (cp instanceof LocalUser) {
			LocalUser localPeer = (LocalUser) cp;
			localPeer.directory = directory;
			localPeer.localStore = localStore;
			return localPeer;
		}

		throw new IOException("Invalid peer type");
	}

	private void writeObject(ObjectOutputStream out) throws IOException,
			ClassNotFoundException {
		peerIds = new ArrayList<Id>();
		for (RemoteUser cp : peers.values()) {
			peerIds.add(cp.getId());
		}

		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();

		connections = new ArrayList<SslConnection>();
		peers = new HashMap<Id, RemoteUser>();
	}

	public void restoreClients() {
		if (peerIds == null) {
			logger.warn("No clients to restore");
			return;
		}

		for (Id clientId : peerIds.toArray(new Id[] {})) {
			RemoteUser cp = new RemoteUser(clientId, directory);

			try {
				RemoteUser cpRestored = cp.restore();
				peers.put(cpRestored.getId(), cpRestored);
			} catch (FileNotFoundException e) {
				logger.warn("Client file is missing for " + clientId
						+ ", removing reference", e);
				peerIds.remove(clientId);
			} catch (IOException e) {
				logger.warn("Could not restore client " + clientId, e);
			} catch (ClassNotFoundException e) {
				logger.warn("Could not restore client " + clientId, e);
			}
		}
	}

	// EXTERNAL STATE CHANGE

	@Override
	public void stateChanged(ChangeEvent ce) {
		Object obj = ce.getSource();

		if (obj instanceof SslConnection) {
			SslConnection sslPeer = (SslConnection) obj;
			if (!sslPeer.isConnected()) {
				connections.remove(obj);
				logger.trace("SSL peer is no longer active ("
						+ sslPeer.getAlias() + ")");
			}
		}
	}

	/**
	 * @todo Better in backup strategy folder?
	 * @return
	 */
	public boolean hasFixedChunkSize() {
		return true;
	}

	public boolean isRecovering() {
		return recovering;
	}

	public boolean receiveChunkData(Chunk chunk, byte[] data) {

		logger.trace("Received chunk data " + chunk.getId() + " / "
				+ chunk.getOutputSize() + " bytes");

		// Chunk may already exist and be valid, in which case just return.
		if (chunks.has(chunk)) {
			return false;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ChunkData cdata;
		try {
			cdata = ChunkData.retrieve(bais, chunk.getId(), this);
		} catch (Exception e) {
			logger.warn(
					"Could not decode local chunk received from remote peer.",
					e);
			return false;
		}

		// There is an empty reference to the chunk (missing).
		if (chunks.missing(chunk)) {
			writeChunkData(chunk, data, chunks);
			return false;
		}

		// No reference to the chunk exists.

		if (isRecovering()) {
			Backup backup = new Backup(cdata);

			// Restore backup from chunk data
			int backupIdx = backups.indexOf(backup);
			if (backupIdx == -1) {
				backups.add(backup);
			} else {
				// Merge backup with chunk data
				Backup currentBackup = backups.get(backupIdx);
				if (!currentBackup.mergeBackup(backup)) {
					return false;
				}
			}

			writeChunkData(chunk, data, chunks);
			persist();
			return true;
		}

		return false;
	}

	/**
	 * Connect user to another Social Backup system through an SSL connection.
	 */
	public boolean connect(Peer targetPeer) {
		SslConnection sslConn = createSslConnection();
		sslConn.setValidHostPort(targetPeer.getHost(), targetPeer.getPort());

		boolean connected = sslConn.connect();
		tracker.connectAttempt(connected);

		logger.trace(getAlias() + " --connect--> " + targetPeer.getAlias()
				+ " = " + (connected ? "yes" : "no"));

		if (connected) {
			return handleConnection(sslConn);
		}

		return false;
	}

	public SslConnection createSslConnection() {
		return new SslConnection(this, changeDispatcher);
	}

	/** Return the duration that a chunk list remains valid. */
	public ReadableDuration getListDuration() {
		return listDuration;
	}

	public ReadableDuration getStatusDuration() {
		return statusDuration;
	}

	/** Create a strategy for placing available chunks onto peers. */
	public void createPlacingStrategy() {
		logger.debug("Creating placing strategy for " + getBackups().size()
				+ " backups to " + getPeers().size() + " peers");

		PriorityQueue<ChunkComparer> comparer = new PriorityQueue<ChunkComparer>();

		for (Backup backup : getBackups()) {
			BackupStrategy backupStrategy = backup.getStrategy();
			if (backupStrategy == null) {
				backupStrategy = getBackupStrategy();
			}
			backupStrategy.place(backup, this, comparer);
		}

		logger.trace(comparer);

		// Collection<RemotePeer> remotePeers = user.getPeers();
		HashMap<RemoteUser, ChunkList> newChunkLists = new HashMap<RemoteUser, ChunkList>();

		for (ChunkComparer cc : comparer) {
			if (cc.getSuitability() <= 0.0) {
				continue;
			}

			RemoteUser remotePeer = cc.getPeer();
			ChunkList chunkList = newChunkLists.get(remotePeer);
			if (chunkList == null) {
				chunkList = new ChunkList();
				newChunkLists.put(remotePeer, chunkList);
			}

			Chunk chunk = cc.getChunk();
			if (chunkList.canHave(remotePeer, chunk)) {
				chunkList.set(cc.getChunk());
			}
		}

		for (Entry<RemoteUser, ChunkList> entry : newChunkLists.entrySet()) {
			entry.getKey().setChunkList(entry.getValue());
			logger.trace(entry.getKey().getAlias() + " now contains: "
					+ entry.getValue());
		}
	}

	/** Check to see if peers have messages to send or needs sync. */
	public void syncAllPeers() {
		for (RemoteUser peer : getPeers()) {
			syncPeer(peer);
		}
	}

	public void syncPeer(RemoteUser peer) {
		if (peer.needsChunkListing(getListDuration())) {
			peer.sendChunkListing();
		}

		if (peer.needsStatus(getStatusDuration())) {
			peer.send(new StatusMessage(this));
		}

		if (!isConnectedTo(peer) && peer.needsConnect()) {
			connect(peer);
		}
	}

	public boolean isConnectedTo(RemoteUser remotePeer) {
		for (RemoteUser checkPeer : getPeers()) {
			if (remotePeer.getHost().equals(checkPeer.getHost())
					&& remotePeer.getPort() == checkPeer.getPort()
					&& checkPeer.isHandled()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasBackup(Backup backup) {
		ChunkList peerStore = getChunkList();
		for (Id chunkId : backup.getChunkIds()) {
			if (!peerStore.has(chunkId)) {
				return false;
			}
		}
		return true;
	}

	public void receive(Message message, RemoteUser peer) {
		logger.trace("@" + getAlias() + " <- "
				+ message.getClass().getSimpleName() + " <- " + peer.getAlias());

		peer.tracker.messageReceived();

		if (message instanceof StatusMessage) {
			peer.updateStatus((StatusMessage) message);
		} else if (message instanceof ChunkMessage) {
			receiveChunk((ChunkMessage) message, peer);
		} else if (message instanceof ChunkListMessage) {
			receiveLists((ChunkListMessage) message, peer);
		}
	}

	public void receiveLists(ChunkListMessage message, RemoteUser peer) {
		peer.tracker.syncReceived();
		syncReceiverList(message.getReceiverChunks(), peer);
		syncSenderList(message.getSenderChunks(), peer);
	}

	private void syncSenderList(ChunkList senderChunks, RemoteUser peer) {
		ChunkList compare = senderChunks.missing(peer.remoteChunks);
		for (Chunk chunk : compare.getChunks()) {
			peer.send(new ChunkReturnMessage(chunk, this));
		}
	}

	private void syncReceiverList(ChunkList receiverChunks, RemoteUser peer) {
		ChunkList compare = receiverChunks.missing(peer.chunks);
		for (Chunk chunk : compare.getChunks()) {
			peer.send(new ChunkSendMessage(chunk, this));
		}
	}

	public boolean receiveChunk(ChunkMessage message, RemoteUser peer) {
		Chunk chunk = message.getChunk();
		byte[] data = message.getData();

		if (message instanceof ChunkSendMessage) {
			return peer.receiveRemoteChunk(chunk, data);
		} else if (message instanceof ChunkReturnMessage) {
			return receiveChunkData(chunk, data);
		} else {
			return false;
		}
	}

}

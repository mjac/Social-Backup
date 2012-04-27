package com.mjac.socialbackup.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.swing.event.ChangeEvent;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.mjac.socialbackup.ChangeDispatcher;
import com.mjac.socialbackup.msg.DisconnectMessage;
import com.mjac.socialbackup.msg.Message;
import com.mjac.socialbackup.msg.PeerMessage;
import com.mjac.socialbackup.msg.StatusMessage;
import com.mjac.socialbackup.state.Peer;
import com.mjac.socialbackup.state.RemotePeer;
import com.mjac.socialbackup.state.PeerBase;
import com.mjac.socialbackup.state.LocalPeer;

@SuppressWarnings("serial")
public class SslConnection extends PeerBase {
	private static final Logger logger = Logger.getLogger(SslConnection.class);

	transient protected RemotePeer peer;
	transient protected SSLSocket socket;

	transient protected ObjectOutputStream outObj;
	transient protected OutputStream out;

	transient protected ObjectInputStream inObj;
	transient protected InputStream in;

	protected PriorityBlockingQueue<Message> incoming = new PriorityBlockingQueue<Message>();

	protected LocalPeer user;
	protected ChangeDispatcher changeDispatcher;

	protected DateTime lastSent = new DateTime();
	protected DateTime lastReceived = new DateTime();

	public SslConnection(LocalPeer user, ChangeDispatcher changeDispatcher) {
		super();
		this.user = user;
		this.changeDispatcher = changeDispatcher;
	}

	private Certificate getSelfSigned() {
		try {
			return socket.getSession().getPeerCertificates()[0];
		} catch (SSLPeerUnverifiedException e) {
			logger.warn("Peer unverified so certificate missing", e);
			return null;
		}
	}

	/**
	 * Get alias and email from certificate.
	 * 
	 * @todo What happens if hostname is unresolved?
	 */
	// public boolean identify() {
	// X509Certificate xCert = (X509Certificate) getSelfSigned();
	// logger.trace(xCert);
	// X500Principal princ = xCert.getSubjectX500Principal();
	// String rawIdentity = princ.toString();
	//
	// String newAlias = "";
	// String newEmail = "";
	// String newHost = "";
	//
	// String[] identityComponents = StringUtils.split(rawIdentity, ',');
	//
	// for (String component : identityComponents) {
	// String[] nameValue = StringUtils.split(component, '=');
	// if (nameValue.length < 2) {
	// continue;
	// }
	//
	// String name = StringUtils.trimToEmpty(nameValue[0]);
	// String value = StringUtils.trimToEmpty(nameValue[1]);
	//
	// if (name.equals("CN")) {
	// newAlias = value;
	// } else if (name.equals("UID")) {
	// newEmail = value;
	// } else if (name.equals("L")) {
	// newHost = value;
	// }
	// }
	//
	// // Only modify internal class state after everything is validated.
	//
	// if (StringUtils.isEmpty(newAlias)
	// || !ServiceProperties.validAlias(newAlias)) {
	// logger.warn("Peer specified invalid alias");
	// return false;
	// }
	// if (StringUtils.isEmpty(newEmail)
	// || !ServiceProperties.validEmail(newEmail)) {
	// logger.warn("Peer specified invalid email");
	// return false;
	// }
	//
	// if (!StringUtils.isEmpty(newHost)) {
	// try {
	// String[] hostComponents = StringUtils.split(newHost, ':');
	// int newPort = Integer.parseInt(hostComponents[1]);
	// if (ServiceProperties.validHostPort(hostComponents[0], newPort)) {
	// host = hostComponents[0];
	// port = newPort;
	// } else {
	// logger.warn("Peer specified invalid hostname");
	// return false;
	// }
	// } catch (Exception e) {
	// logger.warn("Peer specified invalid port", e);
	// return false;
	// }
	// }
	//
	// alias = newAlias;
	// email = newEmail;
	//
	// return true;
	// }

	public PublicKey getPublicKey() {
		Certificate cert = getSelfSigned();
		if (cert == null) {
			return null;
		}
		return cert.getPublicKey();
	}

	/** The address that is currently connected. */
	public InetSocketAddress getCurrentAddress() {
		return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
	}

	/** The address the peer is advertising. */
	public InetSocketAddress getAddress() {
		return new InetSocketAddress(host, port);
	}

	public void setUser(RemotePeer user) {
		this.peer = user;
	}

	public void removeUser() {
		this.peer = null;
	}

	public void setSocket(SSLSocket socket) {
		this.socket = socket;
		openStreams();
	}

	public boolean connect() {
		try {
			SSLSocket newSocket = (SSLSocket) user.getSslContext()
					.getSocketFactory().createSocket(host, port);
			setSocket(newSocket);
			return true;
		} catch (ConnectException ce) {
			return false;
		} catch (Exception e) {
			logger.warn("Could not connect to host", e);
			return false;
		}
	}

	/** Disconnect cleanly by sending a status message if still connected. */
	public void disconnect() {
		logger.debug("Disconnecting " + socket.getInetAddress());

		stopReceiver();

		// Client initiated shutdown.
		if (!receivedDisconnect) {
			send(new DisconnectMessage());
		}

		if (hasAssociatedPeer()) {
			Peer removingPeer = peer;
			peer.stopSender();
			peer.removeHandler();
			removeUser();
			changeDispatcher.stateChanged(new ChangeEvent(removingPeer));
		}

		closeStreams(); // Extraneous, done by socket.close...

		try {
			socket.close();
		} catch (IOException ioe) {
			logger.warn("Issue closing peer socket", ioe);
		}

		changeDispatcher.stateChanged(new ChangeEvent(this));
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	protected void openStreams() {
		try {
			out = socket.getOutputStream();
			outObj = new ObjectOutputStream(out);
			in = socket.getInputStream();
			inObj = new ObjectInputStream(in);
		} catch (IOException e) {
			logger.debug("Streams could not be opened", e);
			closeStreams();
		}
	}

	protected void closeStreams() {
		try {
			if (outObj != null) {
				outObj.close();
			}
		} catch (IOException ioe) {
			logger.debug("Closing stream failed", ioe);
		}
		outObj = null;

		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException ioe) {
			logger.debug("Closing stream failed", ioe);
		}
		out = null;
	}

	public void handshake() {
		logger.trace("Waiting on handshake");
		try {
			socket.startHandshake();
		} catch (IOException e) {
			logger.warn("IOException in starting handshake", e);
		}
	}

	/** Send a message to the peer. */
	public boolean send(Message message) {
		if (outObj == null) {
			return false;
		}
		
		try {
			lastSent = new DateTime();
			outObj.writeObject(message);
			return true;
		} catch (Exception ioe) {
			logger.warn("Could not write message: "
					+ message.getClass().getSimpleName(), ioe);
			return false;
		}
	}

	/** Receive a message from the peer. */
	public Message receive() {
		// "All exceptions are fatal to the InputStream and leave it in an indeterminate state"
		try {
			Object newObj = inObj.readObject();
			if (newObj instanceof Message) {
				lastReceived = new DateTime();
				return (Message) newObj;
			}
		} catch (SocketException se) {
			logger.trace("Socket closed", se);
		} catch (IOException ioe) {
			logger.warn("Could not read message", ioe);
		} catch (ClassNotFoundException cnfe) {
			logger.trace("Invalid class received", cnfe);
			return null;
		}

		disconnect();

		return null;
	}

	Thread receiverThread;
	boolean receiving = false;

	boolean receivedDisconnect = false;

	public void startReceiver() {
		if (receiverThread != null && receiverThread.isAlive()) {
			return;
		}
		receiving = true;
		receiverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (receiving) {
					Message message = receive();
					if (message != null) {
						handleMessage(message);
					}
				}
			}
		});
		receiverThread.start();
	}

	protected void handleMessage(Message message) {

		if (message instanceof PeerMessage) {
			if (hasAssociatedPeer()) {
				peer.receive(message);
			} else {
				logger.warn("Peer message dropped as not authenticated ("
						+ message.getClass().getSimpleName() + ")");
			}
			return;
		}

		// Handle SSL peer messages.

		if (message instanceof DisconnectMessage) {
			logger.trace("Peer is disconnecting");
			receivedDisconnect = true; // So we don't send on an inactive output
			// stream.

			disconnect();
		} else if (message instanceof StatusMessage) {
			updateStatus((StatusMessage) message);
			if (hasAssociatedPeer()) {
				peer.receive(message);
			}
			changeDispatcher.stateChanged(new ChangeEvent(this));
		}

		// Is it a peer message, if so send invalid message if peer is not
		// assigned.
	}

	/**
	 * Send status using peer if present (and actual allocation value),
	 * otherwise send current properties of daemon host.
	 */
	public void sendStatus() {
		if (hasAssociatedPeer()) {
			peer.sendStatus();
			return;
		}

		send(new StatusMessage(user.getAlias(), user.getEmail(),
				user.getHost(), user.getPort(), user.getAllocation()));
	}

	public boolean hasAssociatedPeer() {
		return peer != null;
	}

	/** Get the peer associated with this SSL connection. */
	public RemotePeer getPeer() {
		return peer;
	}

	public DateTime getLastSent() {
		return lastSent;
	}

	public DateTime getLastReceived() {
		return lastReceived;
	}

	public void stopReceiver() {
		receiving = false;
	}

	// Serialisation not allowed for SslConnections, although they extend
	// PeerBase.

	private void writeObject(ObjectOutputStream out) throws IOException,
			ClassNotFoundException {
		throw new NotImplementedException();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		throw new NotImplementedException();
	}
}

package com.mjac.socialbackup.services;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.swing.event.ChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mjac.socialbackup.ChangeDispatcher;
import com.mjac.socialbackup.state.LocalPeer;

public class SslServer extends Thread {
	private static final Logger logger = Logger.getLogger(SslServer.class);

	SSLServerSocket sock;

	protected LocalPeer localPeer;
	protected ChangeDispatcher changeDispatcher;

	public SslServer(LocalPeer servicePeer, ChangeDispatcher changeDispatcher)
			throws Exception {
		this.localPeer = servicePeer;
		this.changeDispatcher = changeDispatcher;

		try {
			SSLServerSocketFactory ssf = servicePeer.getSslContext()
					.getServerSocketFactory();
			logger.trace("Creating SSL server socket");
			sock = (SSLServerSocket) ssf.createServerSocket();
			sock.setNeedClientAuth(true);
			sock.setWantClientAuth(true);
		} catch (Exception e) {
			String problem = "";
			logger.error(problem, e);
			throw new Exception(problem);
		}
	}

	@Override
	public void run() {
		try {
			InetSocketAddress inetSoc = serviceAddress();
			logger.trace("Binding SSL server to " + inetSoc);
			sock.bind(inetSoc);

			changeDispatcher.stateChanged(new ChangeEvent(this));

			while (sock.isBound()) {
				SSLSocket sslSoc = (SSLSocket) sock.accept();
				logger.trace("Client connected from " + sslSoc.getInetAddress());
				SslConnection newPeer = localPeer.createSslConnection();
				newPeer.setSocket(sslSoc);
				localPeer.placePeer(newPeer);
			}
		} catch (SocketException e) {
			logger.info("Server thread stopped", e);
		} catch (IOException e) {
			logger.error(
					"SslServer socket error, server thread stopped unexpectedly",
					e);
		}
	}

	public void close() {
		try {
			if (sock != null) {
				sock.close();
				sock = null;
				changeDispatcher.stateChanged(new ChangeEvent(this));
			}
		} catch (IOException e) {
			logger.error("Error closing socket", e);
		}
	}

	public SocketAddress address() {
		return sock == null ? null : sock.getLocalSocketAddress();
	}

	public boolean isRunning() {
		return address() != null;
	}

	public static String host(String host) {
		try {
			if (StringUtils.isEmpty(host)) {
				host = InetAddress.getLocalHost().getHostAddress();
			}
			return host;
		} catch (Exception e) {
			logger.warn("Host translation failed", e);
			return null;
		}
	}

	public InetSocketAddress serviceAddress() {
		String host = host(localPeer.getHost());
		int port = localPeer.getPort();
		if (host == null || port == 0) {
			return null;
		}
		return new InetSocketAddress(host, port);
	}
}

package com.mjac.socialbackup.unittest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Security;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mjac.socialbackup.ChangeDispatcher;
import com.mjac.socialbackup.RandomisedId;
import com.mjac.socialbackup.actors.LocalUser;
import com.mjac.socialbackup.actors.RemoteUser;
import com.mjac.socialbackup.crypto.KeystoreManager;
import com.mjac.socialbackup.services.SslConnection;
import com.mjac.socialbackup.state.Backup;

public class NetworkTest {
	public static File dir = new File("networktest");

	public static int peerAmount = 2;
	public static int port = 15323;
	public static int peerIndex = 100;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Logger.getRootLogger().setLevel(Level.TRACE);
		Security.addProvider(new BouncyCastleProvider());
		BasicConfigurator.configure();
		dir.mkdir();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileUtils.deleteDirectory(dir);
	}

	private LocalUser you;

	private LocalUser me;

	private String backupText = "Hi, this is my first backup!";

	@Before
	public void setUp() throws Exception {
		me = createRandomPeer(++peerIndex);
		you = createRandomPeer(++peerIndex);
	}

	private LocalUser createRandomPeer(int peerIndex) throws Exception {
		RandomisedId id = new RandomisedId();
		File peerDir = new File(dir.getAbsolutePath() + File.separatorChar + id);
		peerDir.mkdir();

		LocalUser localPeer = new LocalUser(id, peerDir);

		KeystoreManager km = new KeystoreManager(
				new File(peerDir.getAbsolutePath() + File.separatorChar
						+ "keystore.bks"), new char[] {});
		km.blank();

		localPeer.setKeystoreManager(km);
		localPeer.createKeys();
		localPeer.setValidAlias("Host" + peerIndex);
		localPeer.setValidEmail("host" + peerIndex + "@localhost.com");
		localPeer.setValidHostPort("localhost", ++port);

		ChangeDispatcher changeDispatcher = new ChangeDispatcher();
		changeDispatcher.createThread();

		localPeer.setChangeDispatcher(changeDispatcher);
		changeDispatcher.add(localPeer);

		return localPeer;
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(me.getDirectory());
		FileUtils.deleteDirectory(you.getDirectory());
	}

	@Test
	public void connect() throws Exception {
		me.startServer();
		you.startServer();

		Assert.assertTrue(me.connect(you));
	}
	
	private RemoteUser connectFirstPeer(LocalUser user) {
		SslConnection[] sslConns = user.getConnections().toArray(new SslConnection[] {});
		
		Assert.assertTrue(sslConns.length > 0);
		
		RemoteUser meInYou = user.createPeer(sslConns[0], 100);
		Assert.assertTrue(meInYou.isHandled());
		Assert.assertTrue(user.isConnectedTo(meInYou));
		
		return meInYou;
	}
	
	@Test
	public void makeFriends() throws Exception {
		connect();

		connectFirstPeer(me);
		connectFirstPeer(you);
	}

	@Test
	public void createBackup() throws IOException {
		File myBackup = File.createTempFile(me.getAlias(), null);
		FileWriter fw = new FileWriter(myBackup);
		fw.write(backupText);
		fw.close();

		Backup backup = new Backup(myBackup);
		me.addBackup(backup);
	}
	
	@Test
	public void sendBackup() throws Exception {
		makeFriends();
		createBackup();

		me.createPlacingStrategy();
		me.syncAllPeers();
	}
}

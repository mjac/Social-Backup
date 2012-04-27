package com.mjac.socialbackup.unittest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.JUnit4;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.RandomisedId;
import com.mjac.socialbackup.crypto.KeystoreManager;
import com.mjac.socialbackup.state.Backup;
import com.mjac.socialbackup.state.LocalPeer;
import com.mjac.socialbackup.test.Profiler;

public class NetworkTest {
	public Collection<LocalPeer> localPeers;
	public static Random random = new Random();
	public static File dir = new File("networktest");
	
	public static int peerAmount = 2;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		BasicConfigurator.configure();
		dir.mkdir();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileUtils.deleteDirectory(dir);
	}

	@Before
	public void setUp() throws Exception {
		localPeers = new ArrayList<LocalPeer>();
		for (int i = 0; i < peerAmount; ++i) {
			LocalPeer localPeer = new LocalPeer(new RandomisedId());
			File peerDir = new File(dir.getAbsolutePath() + File.separatorChar + localPeer.getId());
			peerDir.mkdir();
			localPeer.setDirectory(peerDir);
			KeystoreManager km = new KeystoreManager(new File(peerDir.getAbsolutePath()
					+ File.separatorChar + "keystore.bks"), new char[] {});
			km.blank();
			localPeer.setKeystoreManager(km);
			localPeer.createKeys();
			localPeer.setValidAlias("Host" + i);
			localPeer.setValidEmail("host" + i + "@localhost.com");
			localPeer.setValidHostPort("localhost", 20000 + i);
			localPeers.add(localPeer);
		}
	}

	@After
	public void tearDown() throws Exception {
		for (LocalPeer peer : localPeers) {
			FileUtils.deleteDirectory(peer.getDirectory());
		}
	}

	@Test
	public void connect()
	{
		for (LocalPeer localPeer : localPeers) {
			Assert.assertTrue(localPeer.startService());
		}
	}
}

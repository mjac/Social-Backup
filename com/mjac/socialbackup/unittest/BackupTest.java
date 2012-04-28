package com.mjac.socialbackup.unittest;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mjac.socialbackup.RandomisedId;
import com.mjac.socialbackup.crypto.KeystoreManager;
import com.mjac.socialbackup.state.Backup;
import com.mjac.socialbackup.state.LocalPeer;
import com.mjac.socialbackup.test.Profiler;

public class BackupTest {
	public static LocalPeer localPeer;
	public static Random random = new Random();
	public static File dir = new File("backuptest");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		dir.mkdir();
		localPeer = new LocalPeer(new RandomisedId(), dir);
		KeystoreManager km = new KeystoreManager(new File(dir.getAbsolutePath()
				+ File.separatorChar + "keystore.bks"), new char[] {});
		km.blank();
		localPeer.setKeystoreManager(km);
		localPeer.createKeys();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileUtils.deleteDirectory(dir);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	static public Collection<Integer> standardChunkSizes(int amt) {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		for (int i = 0; i <= 56; ++i) {
			sizes.add((int) Math.pow(Math.sqrt(2.0), i));
		}
		return sizes;
	}

	// @Test
	// public void

	@Test
	public void sizeRange() throws Exception {
		for (Integer size : standardChunkSizes(56)) {
			backupRandom(size);
		}
	}

	public static void main(String[] t) {
		try {
			setUpBeforeClass();
			BackupTest bt = new BackupTest();
			bt.sizeRange();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static public Backup backupRandom(int size) throws Exception {
		Profiler.getNewInstance("backup").start(size);
		byte[] testBytes = new byte[size];
		random.nextBytes(testBytes);
		Backup backup = backupBytes(testBytes);
		System.out.print(Profiler.getInstance("backup").toString());
		return backup;
	}

	static public Backup backupBytes(byte[] bytes) throws Exception {
		File temp = new File(dir.getAbsolutePath() + File.separatorChar
				+ "temp.backup");
		FileOutputStream fos = new FileOutputStream(temp);
		fos.write(bytes);
		fos.close();
		return backup(temp);
	}

	static public Backup backup(File file) throws Exception {
		Backup newBackup = new Backup(file);
		Profiler.getInstance("backup").instant();
		assertTrue(localPeer.addBackup(newBackup));
		Profiler.getInstance("backup").instant();
		return newBackup;
	}
}

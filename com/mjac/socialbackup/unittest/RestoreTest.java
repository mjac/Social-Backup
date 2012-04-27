package com.mjac.socialbackup.unittest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mjac.socialbackup.state.Backup;
import com.mjac.socialbackup.test.Profiler;

public class RestoreTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BackupTest.setUpBeforeClass();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BackupTest.tearDownAfterClass();
	}

	@Test
	public void sizeRange() throws Exception {
		Profiler.getNewInstance("restore");
		for (Integer size : BackupTest.standardChunkSizes(56)) {
			backupRestore(size);
		}
		System.out.println(Profiler.getInstance("restore").toString());
	}
	
	static public void backupRestore(int size) throws Exception
	{
		Backup backup = BackupTest.backupRandom(size);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Profiler.getInstance("restore").start(size);
		Assert.assertTrue(backup.readToStream(BackupTest.localPeer, baos));
		Profiler.getInstance("restore").instant();
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Profiler.getInstance("restore").instant();
		Assert.assertTrue(backup.verify(bais));
		Profiler.getInstance("restore").instant();
	}
}

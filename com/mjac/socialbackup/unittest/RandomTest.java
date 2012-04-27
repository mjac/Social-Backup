package com.mjac.socialbackup.unittest;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import org.junit.Test;

import com.mjac.socialbackup.test.Profiler;

public class RandomTest {
	static public void fillBytes(Random random, byte[] bytes) {
		random.nextBytes(bytes);
	}

	@Test
	public void secureRandom() throws Exception {
		randomTest(new SecureRandom(), new File("tests" + File.separatorChar
				+ "securerandom.dat"));
	}

	private void randomTest(Random random, File saveTo) throws IOException {
		Profiler.getNewInstance("random");
		for (int i = 0; i < 24; ++i) {
			int bytes = 1 << i;
			byte[] byteBuf = new byte[bytes];
			Profiler.getInstance("random").start(bytes);
			fillBytes(random, byteBuf);
			Profiler.getInstance("random").instant();
		}
		Profiler.getInstance("random").save(saveTo);
	}

	@Test
	public void random() throws Exception {
		randomTest(new Random(), new File("tests" + File.separatorChar
				+ "random.dat"));
	}
}

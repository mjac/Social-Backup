package com.mjac.socialbackup.unittest;

import junit.framework.Assert;

import org.junit.Test;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.RandomisedId;

public class IdTest {
	@Test
	public void encodeDecodeRandom() {
		encodeDecode(new RandomisedId());
	}

	@Test
	public void encodeDecodeLimits() {
		testLong(Long.MAX_VALUE);
		testLong(Long.MIN_VALUE);
		testLong(0L);
	}

	private void encodeDecode(Id a) {
		String aStr = a.toString();
		Id a2 = new Id(aStr);
		Assert.assertEquals(a, a2);
	}

	private void testLong(long test) {
		encodeDecode(new Id(Long.toHexString(test)));
	}
}

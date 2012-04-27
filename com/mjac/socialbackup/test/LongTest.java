package com.mjac.socialbackup.test;

import java.math.BigInteger;

public class LongTest {
	public static void main(String[] args) {
		//System.out.println(Long.dec("ce75fd8b5d37e1ec", 16));
		//System.out.println(Long.decode("#ce75fd8b5d37e1ec"));
		System.out.println(new BigInteger("ce75fd8b5d37e1ec", 16).longValue());

	}
}

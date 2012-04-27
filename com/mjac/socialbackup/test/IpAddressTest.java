package com.mjac.socialbackup.test;

import java.net.InetAddress;

public class IpAddressTest {

	public static void main(String[] args) {
		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			System.out.println("IP:" + thisIp.getHostAddress());
			System.out.println("Hostname:" + thisIp.getHostName());
			System.out.println("Canonical Hostname:" + thisIp.getCanonicalHostName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

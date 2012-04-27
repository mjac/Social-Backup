package com.mjac.socialbackup;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * Other encoding formats "Unlike base 32 and base 64, no special padding is
 * necessary since a full code word is always available."
 * http://tools.ietf.org/html/rfc4648#section-8
 * 
 * @author mjac
 * 
 */

public class Id implements Serializable {
	private static final long serialVersionUID = 1L;

	long id;

	@Override
	public String toString() {
		// Base32 b32 = new Base32();
		// byte[] bytes = getBytes();
		// return b32.encodeToString(bytes); // Only 11 bytes needed.
		return Long.toHexString(id);
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj instanceof Id) {
			return ((Id) obj).id == id;
		} else {
			return false;
		}
	}

	/*
	 * @Override public boolean equals(Id to) { return id == to.id; }
	 */

	public Id(long id) {
		this.id = id;
	}

	/**
	 * This is an integer because long can't be used in Java hashCode without
	 * truncating bits... Well actually I suppose hashcode isn't supposed to be
	 * a unique representation?! Oh well... less redundancy all round...
	 * 
	 * @return
	 */
	public Id(Random random) {
		id = random.nextLong();
	}

	public Id(Random random, Collection<Id> idList) {
		do {
			id = random.nextLong();
		} while (idList.contains(new Id(id)));
	}

	public Id(Collection<Id> idList) {
		id = 0;
		for (Id checkId : idList) {
			if (checkId.id > id) {
				id = checkId.id;
			}
		}
	}

	public Id(String idStr) {
		// Base32 b32 = new Base32();
		// //idStr
		// byte[] bytes = b32.decode(idStr);
		// //byte[] longBytes = new byte[8];
		// ByteBuffer bb = ByteBuffer.wrap(bytes);
		// //bb.put(bytes);
		// //bb.rewind();
		// id = bb.getLong();
		// //id = new BigInteger(idStr, 16).longValue();
		id = new BigInteger(idStr, 16).longValue();
	}

	public byte[] getBytes() {
		byte b[] = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(id);
		return b;
	}

	// static public int generateId() {
	// SecureRandom randomGen = new SecureRandom();
	// return randomGen.nextInt();
	// }
}

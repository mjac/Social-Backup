package com.mjac.socialbackup;

import java.security.SecureRandom;

public class RandomisedId extends Id {
	private static final long serialVersionUID = 1L;

	static private SecureRandom random = new SecureRandom();
	
	public RandomisedId()
	{
		super(random);
	}
}

package com.mjac.socialbackup;

public class Recover {
	// Please provide the keystore (required), config (optional), store metadata (optional)
	// Select a location to recover to
	// Config present?
	// no -> find private key in keystore
	//       alias corresponds to own ID
	// yes -> find private key with config .
	//
	// Metadata present?
	// no -> have to connect to peers and see what they are holding, and try to recover files
	// yes -> we know what files and which are missing
	//
	// Set recovery mode -> peers decide which files user is missing instead of other way round
}

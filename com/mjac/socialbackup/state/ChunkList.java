package com.mjac.socialbackup.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;

import com.mjac.socialbackup.Id;

public class ChunkList implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<Id, Chunk> chunks = new HashMap<Id, Chunk>();
	private long chunkSize = 0;
	
	private DateTime modified = new DateTime();

	public boolean canHave(Peer peer, Chunk chunk)
	{
		return chunk.getOutputSize() + chunkSize <= peer.getAllocation();
	}
	
	public void set(Id id, Chunk chunk) {
		if (has(id)) {
			unset(id);
		}
		chunks.put(id, chunk);
		chunkSize += chunk.getOutputSize();
		modified = new DateTime();
	}

	public void set(Chunk chunk) {
		set(chunk.getId(), chunk);
	}

	public boolean isEmpty()
	{
		return chunks.isEmpty();
	}
	
	public Chunk get(Id id) {
		return chunks.get(id);
	}

	public long getSize() {
		return chunkSize;
	}
	// id -> null (know it exists but content missing)
	// id -> chunk (chunk is there)
	// id missing (no idea what this is)
	public boolean has(Id id) {
		return chunks.containsKey(id) && get(id) != null;
	}

	public boolean has(Chunk chunk) {
		return chunks.containsValue(chunk);
	}

	public boolean missing(Id id) {
		return chunks.containsKey(id) && get(id) == null;
	}

	public boolean missing(Chunk chunk) {
		return missing(chunk.getId());
	}

	public void unset(Id id) {
		Chunk removed = chunks.remove(id);
		if (removed != null) {
			chunkSize -= removed.getOutputSize();
		}
	}
	
	public Set<Id> getIds()
	{
		return chunks.keySet();
	}
	
	public DateTime getModified()
	{
		return modified;
	}
	
	@Override
	public String toString()
	{
		return chunks.keySet().toString();
	}
	
	public Collection<Chunk> getChunks()
	{
		return chunks.values();
	}

	/** Return the chunks present in the given chunk list but not in this chunk list. */
	public ChunkList missing(ChunkList remoteChunks) {
		ChunkList toGet = new ChunkList();
		for (Chunk chunk : remoteChunks.getChunks()) {
			if (!has(chunk.getId()) ) {
				toGet.set(chunk);
			}
		}
		return toGet;
	}
}

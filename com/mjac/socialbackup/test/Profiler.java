package com.mjac.socialbackup.test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class Profiler {
	// protected StringBuilder sb;
	protected static HashMap<String, Profiler> profilers = new HashMap<String, Profiler>();
	
	public class Entry {
		public Object id;
		public DateTime start;
		public ArrayList<DateTime> content = new ArrayList<DateTime>();
		
		public Entry(Object id)
		{
			this.id = id;
			start = new DateTime();
		}
	}

	protected ArrayList<Entry> dateTime = new ArrayList<Entry>();

	public static Profiler getNewInstance(String string) {
		profilers.put(string, new Profiler());
		return getInstance(string);
	}

	public static Profiler getInstance(String string) {
		return profilers.get(string);
	}

	public Profiler() {
	}

	public void start(Object id) {
		dateTime.add(new Entry(id));
	}

	public void instant() {
		dateTime.get(dateTime.size() - 1).content.add(new DateTime());
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Entry arr : dateTime) {
			sb.append(arr.id);
			for (DateTime dt : arr.content) {
				sb.append(' ');
				sb.append(new Duration(arr.start, dt).getMillis());
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public void save(File file) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(toString().getBytes());
		fos.close();
	}
}

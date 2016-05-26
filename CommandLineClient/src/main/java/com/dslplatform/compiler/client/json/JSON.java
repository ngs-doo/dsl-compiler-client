package com.dslplatform.compiler.client.json;

import java.io.IOException;
import java.util.*;

public abstract class JSON {

	private static Object deserializeObject(final JsonReader reader) throws IOException {
		switch (reader.last()) {
			case 'n':
				if (!reader.wasNull()) {
					throw new IOException("Expecting 'null' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
				}
				return null;
			case 't':
				if (!reader.wasTrue()) {
					throw new IOException("Expecting 'true' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
				}
				return true;
			case 'f':
				if (!reader.wasFalse()) {
					throw new IOException("Expecting 'false' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
				}
				return false;
			case '"':
				return reader.readString();
			case '{':
				return deserializeMap(reader);
			case '[':
				return deserializeList(reader);
			default:
				return NumberConverter.deserializeNumber(reader);
		}
	}

	private static ArrayList<Object> deserializeList(final JsonReader reader) throws IOException {
		if (reader.last() != '[') {
			throw new IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		byte nextToken = reader.getNextToken();
		if (nextToken == ']') return new ArrayList<Object>(0);
		final ArrayList<Object> res = new ArrayList<Object>(4);
		res.add(deserializeObject(reader));
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			res.add(deserializeObject(reader));
		}
		if (nextToken != ']') {
			throw new IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
		}
		return res;
	}

	public static Map<String, Object> readMap(final byte[] input, final int length) throws IOException {
		JsonReader reader = new JsonReader(input, length);
		reader.getNextToken();
		return deserializeMap(reader);
	}

	private static LinkedHashMap<String, Object> deserializeMap(final JsonReader reader) throws IOException {
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char) reader.last());
		}
		byte nextToken = reader.getNextToken();
		if (nextToken == '}') return new LinkedHashMap<String, Object>(0);
		final LinkedHashMap<String, Object> res = new LinkedHashMap<String, Object>();
		String key = reader.readString();
		nextToken = reader.getNextToken();
		if (nextToken != ':') {
			throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
		}
		reader.getNextToken();
		res.put(key, deserializeObject(reader));
		while ((nextToken = reader.getNextToken()) == ',') {
			reader.getNextToken();
			key = reader.readString();
			nextToken = reader.getNextToken();
			if (nextToken != ':') {
				throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
			}
			reader.getNextToken();
			res.put(key, deserializeObject(reader));
		}
		if (nextToken != '}') {
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
		}
		return res;
	}
}

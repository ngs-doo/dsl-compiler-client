package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.json.JsonObject;
import java.util.Map;

public class Utils {
	public static JsonObject toJson(final Map<String, String> map) {
		final JsonObject json = new JsonObject();
		for(final Map.Entry<String, String> kv : map.entrySet()) {
			json.add(kv.getKey(), kv.getValue());
		}
		return json;
	}
}

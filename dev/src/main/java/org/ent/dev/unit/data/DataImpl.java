package org.ent.dev.unit.data;

import java.util.HashMap;
import java.util.Map;

public class DataImpl implements Data {

	private final Map<String, Object> properties;

	public DataImpl() {
		this.properties = new HashMap<>();
	}

	public DataImpl(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

}

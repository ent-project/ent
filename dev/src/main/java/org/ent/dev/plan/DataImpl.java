package org.ent.dev.plan;

import java.util.HashMap;
import java.util.Map;

import org.ent.dev.plan.DataProperties.PropNet;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropSeed;
import org.ent.dev.plan.DataProperties.PropSerialNumber;
import org.ent.dev.plan.DataProperties.PropSourceInfo;
import org.ent.dev.plan.DataProperties.PropStepsExamResult;
import org.ent.dev.unit.Data;

public class DataImpl implements Data,
		PropNet,
		PropSeed,
		PropReplicator,
		PropSerialNumber,
		PropStepsExamResult,
		PropSourceInfo {

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

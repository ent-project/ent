package org.ent.dev.unit;

import java.util.Map;

public class DataProxy implements Data {

	private Data delegate;

	public DataProxy() {
	}

	public DataProxy(Data delegate) {
		this.delegate = delegate;
	}

	public void setDelegate(Data delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object getProperty(String key) {
		return delegate.getProperty(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		delegate.setProperty(key, value);
	}

	@Override
	public Map<String, Object> getProperties() {
		return delegate.getProperties();
	}

}

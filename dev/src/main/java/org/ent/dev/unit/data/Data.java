package org.ent.dev.unit.data;

import java.util.Map;

public interface Data {

	Object getProperty(String key);

	void setProperty(String key, Object value);

	Map<String, Object> getProperties();

}

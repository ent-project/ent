package org.ent.gui.pref;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntPreferences extends AbstractPreferences {

	private static final Logger log = LoggerFactory.getLogger(EntPreferences.class);

	private Map<String, String> propMap;

	public EntPreferences() {
		super(null, "");
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
		log.trace("syncSpi");

		Map<String, String> newPropMap = new TreeMap<>();

		Path file = getPreferencesFile();
		if (Files.exists(file)) {
			Properties properties = new Properties();
				try (InputStream inputStream = Files.newInputStream(file)) {
					properties.load(inputStream);
				} catch (IOException e) {
					throw new BackingStoreException(e);
				}
			properties.entrySet().stream().forEach(e -> newPropMap.put(e.getKey().toString(), e.getValue().toString()));
		}
		propMap = newPropMap;
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
		log.trace("flushSpi");

		Properties properties = new Properties();
		properties.putAll(propMap);
		try (OutputStream outputStream = Files.newOutputStream(getPreferencesFile())) {
			properties.store(outputStream, "preferences");
		} catch (IOException e) {
			throw new BackingStoreException(e);
		}
	}

	@Override
	protected String getSpi(String key) {
		log.trace("getSpi '{}'", key);

		if (propMap == null) {
			try {
				sync();
			} catch (BackingStoreException e) {
				log.error("unable to sync", e);
			}
		}
		return propMap.get(key);
	}

	@Override
	protected void putSpi(String key, String value) {
		log.trace("putSpi '{}'='{}'", key, value);
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}

		if (propMap == null) {
			try {
				sync();
			} catch (BackingStoreException e) {
				log.error("unable to sync", e);
			}
		}

		propMap.put(key, value);
		try {
			flush();
		} catch (BackingStoreException e) {
			log.error("Unable to flush after putting " + key, e);
		}
	}

	private Path getPreferencesFile() {
		return Paths.get("preferences.properties");
	}

	@Override
	protected AbstractPreferences childSpi(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void removeSpi(String arg0) {
		throw new UnsupportedOperationException();
	}

}

package org.ent.net;

import java.util.EnumMap;
import java.util.Map;

/**
 * ArrowDirection is an enumeration that is used to distinguish the childs of a node.
 * It allows to loop over children.
 */
public enum ArrowDirection {
	LEFT, RIGHT;

	public static final Map<ArrowDirection, String> ARROW_SYMBOLS = new EnumMap<>(ArrowDirection.class);
	static {
		ARROW_SYMBOLS.put(ArrowDirection.LEFT, "↙");
		ARROW_SYMBOLS.put(ArrowDirection.RIGHT, "↘");
	}

	public static final Map<ArrowDirection, String> ARROW_SYMBOLS_ASCII = new EnumMap<>(ArrowDirection.class);
	static {
		ARROW_SYMBOLS_ASCII.put(ArrowDirection.LEFT, "/");
		ARROW_SYMBOLS_ASCII.put(ArrowDirection.RIGHT, "\\");
	}
}

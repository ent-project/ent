package org.ent.net;

import java.util.EnumMap;

import org.ent.net.node.BNode;
import org.ent.net.node.UNode;

/**
 * ArrowDirection is an enumeration that is used to distinguish the childs of a node.
 * It allows to loop over children more generically.
 *
 * Depending on the node class, only certain arrow directions are compatible:
 * A binary node ({@link BNode}) can have a {@link #LEFT} child and a {@link #RIGHT} child.
 * A unary node ({@link UNode}) only has one {@link #DOWN} child.
 */
public enum ArrowDirection {
	DOWN, LEFT, RIGHT;

	public static final EnumMap<ArrowDirection, String> ARROW_SYMBOLS = new EnumMap<>(ArrowDirection.class);
	static {
		ARROW_SYMBOLS.put(ArrowDirection.DOWN, "↓");
		ARROW_SYMBOLS.put(ArrowDirection.LEFT, "↙");
		ARROW_SYMBOLS.put(ArrowDirection.RIGHT, "↘");
	}

	public static final EnumMap<ArrowDirection, String> ARROW_SYMBOLS_ASCII = new EnumMap<>(ArrowDirection.class);
	static {
		ARROW_SYMBOLS_ASCII.put(ArrowDirection.DOWN, "|");
		ARROW_SYMBOLS_ASCII.put(ArrowDirection.LEFT, "/");
		ARROW_SYMBOLS_ASCII.put(ArrowDirection.RIGHT, "\\");
	}
}

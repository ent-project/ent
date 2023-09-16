package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

import static org.ent.net.ArrowDirection.ARROW_SYMBOLS;

public class PrimaryAccessor implements Accessor {

	private final ArrowDirection direction;

	private final int code;

    private final String shortName;

	public PrimaryAccessor(ArrowDirection direction) {
		this.direction = direction;
		this.code = 0b10 | (direction == ArrowDirection.RIGHT ? 0b01 : 0);
		this.shortName = ARROW_SYMBOLS.get(ArrowDirection.LEFT) + ARROW_SYMBOLS.get(direction);
	}

	@Override
	public Arrow get(Node base, Permissions permissions) {
		Node parameters = base.getLeftChild(permissions);
		return parameters.getArrow(direction);
	}

	public ArrowDirection getDirection() {
		return direction;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String toString() {
		return getShortName();
	}

}

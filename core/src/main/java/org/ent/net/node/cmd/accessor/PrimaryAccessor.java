package org.ent.net.node.cmd.accessor;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class PrimaryAccessor implements Accessor {

	private final ArrowDirection direction;

	private final int code;

    private final String shortName;

	public PrimaryAccessor(ArrowDirection direction) {
		this.direction = direction;
		this.code = 0b10 | (direction == ArrowDirection.RIGHT ? 0b01 : 0);
		this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction);
	}

	@Override
	public Arrow get(Node base, Ent ent, Purview purview) {
		return ent.advanceWithPortals(base.getLeftChild(purview), direction);
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
}

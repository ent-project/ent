package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;

public class PrimaryAccessor implements Accessor {

    private final ArrowDirection direction;

	private final int code;

	private final String shortName;

    private final String shortNameAscii;

    public PrimaryAccessor(ArrowDirection direction) {
		this.direction = direction;
		this.code = 0b10 | (direction == ArrowDirection.RIGHT ? 0b01 : 0);
		this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction);
		this.shortNameAscii = ArrowDirection.ARROW_SYMBOLS_ASCII.get(direction);
	}

	@Override
	public Arrow get(Arrow arrow, Purview purview) {
		return arrow.getTarget(purview).getArrow(this.direction);
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
	public String getShortNameAscii() {
		return shortNameAscii;
	}
}

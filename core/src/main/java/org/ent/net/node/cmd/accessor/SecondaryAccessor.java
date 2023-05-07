package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;

public class SecondaryAccessor implements Accessor {

    private final ArrowDirection direction1;

    private final ArrowDirection direction2;

	private final int code;

    private final String shortName;

    private final String shortNameAscii;

    public SecondaryAccessor(ArrowDirection direction1, ArrowDirection direction2) {
		this.direction1 = direction1;
		this.direction2 = direction2;
		this.code = 0b100 | (direction1 == ArrowDirection.RIGHT ? 0b001 : 0) | (direction2 == ArrowDirection.RIGHT ? 0b010 : 0);
		this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction1) + ArrowDirection.ARROW_SYMBOLS.get(direction2);
		this.shortNameAscii = ArrowDirection.ARROW_SYMBOLS_ASCII.get(direction1)
				+ ArrowDirection.ARROW_SYMBOLS_ASCII.get(direction2);
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public Arrow get(Arrow arrow, Purview purview) {
		return arrow.getTarget(purview).getArrow(direction1).getTarget(purview).getArrow(direction2);
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

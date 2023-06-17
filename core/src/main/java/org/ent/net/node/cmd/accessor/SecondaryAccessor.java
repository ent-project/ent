package org.ent.net.node.cmd.accessor;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class SecondaryAccessor implements Accessor {

    private final ArrowDirection direction1;

    private final ArrowDirection direction2;

	private final int code;

    private final String shortName;

    public SecondaryAccessor(ArrowDirection direction1, ArrowDirection direction2) {
		this.direction1 = direction1;
		this.direction2 = direction2;
		this.code = 0b100 | (direction1 == ArrowDirection.RIGHT ? 0b001 : 0) | (direction2 == ArrowDirection.RIGHT ? 0b010 : 0);
		this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction1)
				+ ArrowDirection.ARROW_SYMBOLS.get(direction2);
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public Arrow get(Node base, Ent ent, Purview purview) {
		Node node1 = base.getLeftChild(purview);
		Arrow arrow1 = ent.getArrowMaybeThroughPortal(node1, direction1, purview);
		Node node2 = arrow1.getTarget(purview);
		return ent.getArrowMaybeThroughPortal(node2, direction2, purview);
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

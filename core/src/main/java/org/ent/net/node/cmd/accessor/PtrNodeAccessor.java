package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class PtrNodeAccessor implements Accessor<Node> {

    private final ArrowDirection direction;

    private final String shortName;

    private final String shortNameAscii;

	public PtrNodeAccessor(ArrowDirection direction) {
		this.direction = direction;
		this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction);
		this.shortNameAscii = ArrowDirection.ARROW_SYMBOLS_ASCII.get(direction);
	}

	@Override
	public Optional<Node> get(Node node, Purview purview) {
		return node.getArrowMaybe(direction).map(childArrow -> childArrow.getTarget(purview));
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

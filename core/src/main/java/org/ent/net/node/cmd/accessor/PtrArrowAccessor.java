package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Manner;
import org.ent.net.node.Node;

public class PtrArrowAccessor implements Accessor<Arrow> {

    private final ArrowDirection direction1;

    private final ArrowDirection direction2;

    private final String shortName;

    private final String shortNameAscii;

    public PtrArrowAccessor(ArrowDirection direction1, ArrowDirection direction2) {
		this.direction1 = direction1;
		this.direction2 = direction2;
		this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction1) + ArrowDirection.ARROW_SYMBOLS.get(direction2);
		this.shortNameAscii = ArrowDirection.ARROW_SYMBOLS_ASCII.get(direction1)
				+ ArrowDirection.ARROW_SYMBOLS_ASCII.get(direction2);
	}

	@Override
	public Optional<Arrow> get(Node node, Manner manner) {
        return node.getArrowMaybe(this.direction1).flatMap(arrow -> {
        	Node child = arrow.getTarget(manner);
        	return child.getArrowMaybe(this.direction2);
        });
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

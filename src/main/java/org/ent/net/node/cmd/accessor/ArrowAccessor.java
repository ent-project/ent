package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.NetController;
import org.ent.net.node.Node;

public class ArrowAccessor implements Accessor<Arrow> {

    private final ArrowDirection direction;

    private final String shortName;

    public ArrowAccessor(ArrowDirection direction) {
		this.direction = direction;
		this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction);
	}

	@Override
	public Optional<Arrow> get(NetController controller, Node node) {
        return node.getArrowMaybe(this.direction);
	}

	@Override
	public String getShortName() {
		return shortName;
	}
}

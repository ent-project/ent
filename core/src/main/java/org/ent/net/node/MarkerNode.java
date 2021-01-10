package org.ent.net.node;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MarkerNode extends Node {

	public static final String MARKER_NODE_SYMBOL = "●";

	public static final String MARKER_NODE_SYMBOL_ASCII = "#";

	public MarkerNode(Net net) {
		super(net);
	}

	@Override
    public List<Arrow> getArrows() {
    	return Collections.emptyList();
    }

	@Override
	public Arrow getArrow(ArrowDirection arrowDirection) {
        throw new UnsupportedOperationException();
	}

	@Override
	public Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection) {
        throw new UnsupportedOperationException();
	}

    @Override
    public String toString() {
        return MARKER_NODE_SYMBOL;
    }

	@Override
	public <T> T instanceOf(Function<CNode, T> cNodeCase, Function<UNode, T> uNodeCase, Function<BNode, T> bNodeCase) {
		throw new AssertionError("No MarkerNode expected");
	}
}

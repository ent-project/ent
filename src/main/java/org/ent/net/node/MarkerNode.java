package org.ent.net.node;

import java.util.List;
import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;

public class MarkerNode extends Node {

    @Override
    public List<Arrow> getArrows() {
        throw new UnsupportedOperationException();
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
	public boolean isInternal() {
        throw new UnsupportedOperationException();
	}

    @Override
    public String toString() {
        return "###";
    }
}

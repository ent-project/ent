package org.ent.net.io.parser;

import com.google.common.annotations.VisibleForTesting;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

public class ValueNodeTemplate implements NodeTemplate {
    private final int value;

    public ValueNodeTemplate(int value) {
        this.value = value;
    }

    @VisibleForTesting
    public int getValue() {
        return value;
    }

    @Override
    public Node generateNode(Net net) throws ParserException {
        return net.newNode(value);
    }

    @Override
    public NodeTemplate getChild(ArrowDirection arrowDirection) {
        return this;
    }

    @Override
    public void setChild(ArrowDirection arrowDirection, NodeTemplate child) {
        throw new IllegalArgumentException();
    }
}
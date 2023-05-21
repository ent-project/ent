package org.ent.net.node;

import com.google.common.annotations.VisibleForTesting;
import org.ent.Profile;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.veto.Veto;

import java.util.List;

public interface Node {

	Net getNet();
	void setNet(Net net);

	Hub getHub();

	void setHub(Hub hub);

	List<Arrow> getArrows();

	Arrow getArrow(ArrowDirection arrowDirection);

	Arrow getLeftArrow();

	Arrow getRightArrow();

	Node getLeftChild(Purview purview);

	@VisibleForTesting
	default Node getLeftChild() {
		Profile.verifyTestProfile();
		return getLeftChild(Purview.DIRECT);
	}

	default boolean hasProperLeftChild() {
		return getLeftChild(Purview.DIRECT) != this;
	}

	default boolean hasProperRightChild() {
		return getRightChild(Purview.DIRECT) != this;
	}

	void setLeftChild(Node child, Purview purview);

	@VisibleForTesting
	default void setLeftChild(Node child) {
		Profile.verifyTestProfile();
		setLeftChild(child, Purview.DIRECT);
	}

	Node getRightChild(Purview purview);

	@VisibleForTesting
	default Node getRightChild() {
		Profile.verifyTestProfile();
		return getRightChild(Purview.DIRECT);
	}

	void setRightChild(Node child, Purview purview);

	@VisibleForTesting
	default void setRightChild(Node child) {
		Profile.verifyTestProfile();
		setRightChild(child, Purview.DIRECT);
	}

	int getValue();

	void setValue(int value);

	default void setCommand(Command command) {
		setValue(command.getValue());
	}

	default void setVeto(Veto value) {
		setValue(value.getValue());
	}

	boolean isUnaryNode();

	boolean isCommandNode();

	boolean isMarkerNode();

	NodeType getNodeType();

	default Command getCommand() {
		return Commands.getByValue(getValue());
	}
}

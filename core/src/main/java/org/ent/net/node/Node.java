package org.ent.net.node;

import com.google.common.annotations.VisibleForTesting;
import org.ent.permission.Permissions;
import org.ent.Profile;
import org.ent.permission.WriteFacet;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.permission.PermissionsViolatedException;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.veto.Veto;

import java.util.List;
import java.util.Optional;

import static org.ent.permission.Permissions.DOUBLE_CHECK_PERMISSIONS;

/**
 * Binary node.
 */
public class Node {
	private Net net;
	private Hub hub;
	private int index;

	private int value;
	private Hub leftChildHub;
	private Hub rightChildHub;

	private final Arrow leftArrow = new BNodeLeftArrow();
	private final Arrow rightArrow = new BNodeRightArrow();
	private final List<Arrow> arrows = List.of(leftArrow, rightArrow);

	public Node(Net net, int value, Node leftChild, Node rightChild) {
		this.net = net;
		this.hub = new Hub(this);
		this.value = value;
		initialize(leftChild, rightChild);
	}

	public Node(Net net, int value, Optional<Node> leftChild, Optional<Node> rightChild) {
		this.net = net;
		this.hub = new Hub(this);
		this.value = value;
		initialize(leftChild.orElse(this), rightChild.orElse(this));
	}

	public Node(Net net, Node leftChild, Node rightChild) {
		this(net, 0, leftChild, rightChild);
	}

	public Node(Net net, Node leftChild) {
		this.net = net;
		this.hub = new Hub(this);
		initialize(leftChild, this);
	}

	public Node(Net net) {
		this.net = net;
		this.hub = new Hub(this);
		initialize(this, this);
	}

	public Node(Net net, int value) {
		this.net = net;
		this.hub = new Hub(this);
		this.value = value;
		initialize(this, this);
	}

	private void initialize(Node leftChild, Node rightChild) {
		this.leftChildHub = leftChild.getHub();
		this.rightChildHub = rightChild.getHub();
	}

	public Net getNet() {
		return net;
	}

	public void setNet(Net net) {
		this.net = net;
	}

	public Hub getHub() {
		return hub;
	}

	public void setHub(Hub hub) {
		this.hub = hub;
	}

	public Node getChild(ArrowDirection direction, Permissions permissions) {
		return switch (direction) {
			case LEFT -> getLeftChild(permissions);
			case RIGHT -> getRightChild(permissions);
		};
	}

	public boolean hasProperLeftChild(Permissions permissions) {
		return getLeftChild(permissions) != this;
	}

	public boolean hasProperRightChild(Permissions permissions) {
		return getRightChild(permissions) != this;
	}

	public void setChild(ArrowDirection direction, Node child, Permissions permissions) {
		switch (direction) {
			case LEFT -> setLeftChild(child, permissions);
			case RIGHT -> setRightChild(child, permissions);
		}
	}

	@VisibleForTesting
	public void setLeftChild(Node child) {
		Profile.verifyTestProfile();
		setLeftChild(child, Permissions.DIRECT);
	}

	@VisibleForTesting
	public void setRightChild(Node child) {
		Profile.verifyTestProfile();
		setRightChild(child, Permissions.DIRECT);
	}

	@VisibleForTesting
	public int getValue() {
		Profile.verifyTestProfile();
		return getValue(Permissions.DIRECT);
	}

	@VisibleForTesting
	public void setValue(int value) {
		Profile.verifyTestProfile();
		setValue(value, Permissions.DIRECT);
	}

	@VisibleForTesting
	public void setCommand(Command command) {
		Profile.verifyTestProfile();
		setValue(command.getValue(), Permissions.DIRECT);
	}

	@VisibleForTesting
	public void setVeto(Veto value) {
		Profile.verifyTestProfile();
		setValue(value.getValue(), Permissions.DIRECT);
	}

	public Command getCommand() {
		return Commands.getByValue(getValue());
	}

	public Node getLeftChild(Permissions permissions) {
		return leftArrow.getTarget(permissions);
	}

	@VisibleForTesting
	public Node getLeftChild() {
		Profile.verifyTestProfile();
		return getLeftChild(Permissions.DIRECT);
	}

	public void setLeftChild(Node child, Permissions permissions) {
		leftArrow.setTarget(child, permissions);
	}

	public Node getRightChild(Permissions permissions) {
		return rightArrow.getTarget(permissions);
	}

	@VisibleForTesting
	public Node getRightChild() {
		Profile.verifyTestProfile();
		return getRightChild(Permissions.DIRECT);
	}

	public void setRightChild(Node child, Permissions permissions) {
		rightArrow.setTarget(child, permissions);
	}

	public Arrow getLeftArrow() {
		return leftArrow;
	}

	public Arrow getRightArrow() {
		return rightArrow;
	}

	public List<Arrow> getArrows() {
		return arrows;
	}

	public Arrow getArrow(ArrowDirection arrowDirection) {
		return switch (arrowDirection) {
			case LEFT -> leftArrow;
			case RIGHT -> rightArrow;
		};
	}

	final public int getValue(Permissions permissions) {
		net.event(permissions).getValue(this);
		return value;
	}

	public void setValue(int value, Permissions permissions) {
		if (DOUBLE_CHECK_PERMISSIONS) {
			if (permissions.noWrite(this, WriteFacet.VALUE)) {
				throw new PermissionsViolatedException();
			}
		}
		net.event(permissions).setValue(this, this.value, value);
		this.value = value;
	}

	public boolean isUnaryNode() {
		return rightChildHub.getNode() == this && leftChildHub.getNode() != this;
	}

	public boolean isLeafNode() {
		return leftChildHub.getNode() == this && rightChildHub.getNode() == this;
	}

	public boolean isMarkerNode() {
		return false;
	}

	public NodeType getNodeType() {
		if (rightChildHub.getNode() == this) {
			if (leftChildHub.getNode() == this) {
				return NodeType.COMMAND_NODE;
			} else {
				return NodeType.UNARY_NODE;
			}
		} else {
			return NodeType.BINARY_NODE;
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setName(String name) {
		net.setName(this, name);
	}

	public String getName() {
		return net.getName(this);
	}

	private class BNodeLeftArrow implements Arrow {
		@Override
		public ArrowDirection getDirection() {
			return ArrowDirection.LEFT;
		}

		@Override
		public Node getOrigin() {
			return Node.this;
		}

		public Node getTarget(Permissions permissions) {
			net.event(permissions).calledGetChild(Node.this, ArrowDirection.LEFT);
			return leftChildHub.getNode();
		}

		public void setTarget(Node target, Permissions permissions) {
			if (DOUBLE_CHECK_PERMISSIONS) {
				// At this point, we do not know if the caller is eval_flow.
				// If it is not, the following test is too lenient, so this
				// 	double check may not be a full verification of valid permissions.
				if (permissions.noWrite(Node.this, WriteFacet.ARROW) &&
						permissions.noExecute(Node.this)) {
					throw new PermissionsViolatedException();
				}
			}
			net.event(permissions).calledSetChild(Node.this, ArrowDirection.LEFT, target);
			doSetTarget(target);
		}

		private void doSetTarget(Node target) {
			leftChildHub = target.getHub();
		}
	}

	private class BNodeRightArrow implements Arrow {
		@Override
		public ArrowDirection getDirection() {
			return ArrowDirection.RIGHT;
		}

		@Override
		public Node getOrigin() {
			return Node.this;
		}

		public Node getTarget(Permissions permissions) {
			net.event(permissions).calledGetChild(Node.this, ArrowDirection.RIGHT);
			return rightChildHub.getNode();
		}

		public void setTarget(Node target, Permissions permissions) {
			net.event(permissions).calledSetChild(Node.this, ArrowDirection.RIGHT, target);
			doSetTarget(target);
		}

		private void doSetTarget(Node target) {
			rightChildHub = target.getHub();
		}
	}
}

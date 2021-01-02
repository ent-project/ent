package org.ent.net.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.cmd.Command;

/**
 * A command node.
 *
 * This node has no children. It is used to indicate a certain command that is to be
 * executed.
 */
public class CNode extends Node {

	private final Command command;

	public CNode(Net net, Command command) {
		super(net);
		this.command = command;
	}

	public Command getCommand() {
		return command;
	}

	@Override
	public List<Arrow> getArrows() {
		return Collections.emptyList();
	}

	@Override
	public Arrow getArrow(ArrowDirection arrowDirection) {
		throw new IllegalArgumentException();
	}

	@Override
	public Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection) {
		return Optional.empty();
	}

}

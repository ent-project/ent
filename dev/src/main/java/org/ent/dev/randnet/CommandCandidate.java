package org.ent.dev.randnet;

import org.ent.net.node.cmd.Command;

public class CommandCandidate {

	private final Command command;

	private final double weight;

	public CommandCandidate(Command command, double weight) {
		this.command = command;
		this.weight = weight;
	}

	public Command getCommand() {
		return command;
	}

	public double getWeight() {
		return weight;
	}
}
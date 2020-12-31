package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;

class CommandNodeTemplate implements NodeTemplate {
    private final String commandName;

    String getCommandName() {
		return commandName;
	}

	public CommandNodeTemplate(String commandName) {
        this.commandName = commandName;
    }

	@Override
	public Node generateNode(Net net) throws ParserException {
        Command command = CommandFactory.getByName(commandName);
        if (command == null) {
            throw new ParserException("Unknown command: '" + commandName + "'");
        }
		return net.newCNode(command);
	}

	@Override
	public NodeTemplate getChild(ArrowDirection arrowDirection) {
		throw new IllegalArgumentException();
	}
}
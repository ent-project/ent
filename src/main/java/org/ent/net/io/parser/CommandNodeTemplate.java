package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.NetController;
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
	public Node generateNode(NetController controller, Node childPlaceholder) throws ParserException {
        Command command = CommandFactory.getByName(commandName);
        if (command == null) {
            throw new ParserException("Unknown command: '" + commandName + "'");
        }
		return controller.newCNode(command);
	}

	@Override
	public NodeTemplate getChild(ArrowDirection arrowDirection) {
		throw new IllegalArgumentException();
	}
}
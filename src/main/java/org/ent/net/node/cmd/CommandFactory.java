package org.ent.net.node.cmd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandFactory {

	private final static List<Command> allCommands = Arrays.asList(new NopCommand());

	private final static Map<String, Command> commandsByName = buildCommandsByName();

	private static Map<String, Command> buildCommandsByName() {
		Map<String, Command> result = new HashMap<>();
		for (Command command : allCommands) {
			String name = command.getShortName();
			if (result.containsKey(name)) {
				throw new AssertionError("Duplicate command name: " + name);
			}
			result.put(name, command);
		}
		return result;
	}

	public static Command getByName(String name) {
		return commandsByName.get(name);
	}

}

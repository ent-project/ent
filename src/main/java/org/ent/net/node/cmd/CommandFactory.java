package org.ent.net.node.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.ArrowAccessor;
import org.ent.net.node.cmd.accessor.NodeAccessor;
import org.ent.net.node.cmd.accessor.PtrArrowAccessor;
import org.ent.net.node.cmd.accessor.PtrNodeAccessor;
import org.ent.net.node.cmd.accessor.PtrPtrNodeAccessor;

public class CommandFactory {

	private final static List<Command> allCommands = collectAllCommands();

	private final static Map<String, Command> commandsByName = buildCommandsByName();

	private static List<Command> collectAllCommands() {
		List<Command> result = new ArrayList<>();
		result.add(new NopCommand());
		for (ArrowDirection left : ArrowDirection.values()) {
			result.add(createSetCommandL(left));
			for (ArrowDirection right : ArrowDirection.values()) {
				result.add(createSetCommandLR(left, right));
			}
			for (ArrowDirection right1 : ArrowDirection.values()) {
				for (ArrowDirection right2 : ArrowDirection.values()) {
					result.add(createSetCommandLRR(left, right1, right2));
				}
			}
		}
		for (ArrowDirection left1 : ArrowDirection.values()) {
			for (ArrowDirection left2 : ArrowDirection.values()) {
				result.add(createSetCommandLL(left1, left2));
				for (ArrowDirection right : ArrowDirection.values()) {
					result.add(createSetCommandLLR(left1, left2, right));
				}
				for (ArrowDirection right1 : ArrowDirection.values()) {
					for (ArrowDirection right2 : ArrowDirection.values()) {
						result.add(createSetCommandLLRR(left1, left2, right1, right2));
					}
				}
			}
		}
		return result;
	}

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

	public static Command createSetCommandL(ArrowDirection left) {
		return new BiCommand<Arrow, Node>(new ArrowAccessor(left), new NodeAccessor(), new SetOperation());
	}

	public static Command createSetCommandLL(ArrowDirection left1, ArrowDirection left2) {
		return new BiCommand<Arrow, Node>(new PtrArrowAccessor(left1, left2), new NodeAccessor(), new SetOperation());
	}

	public static Command createSetCommandLR(ArrowDirection left, ArrowDirection right) {
		return new BiCommand<Arrow, Node>(new ArrowAccessor(left), new PtrNodeAccessor(right), new SetOperation());
	}

	public static Command createSetCommandLRR(ArrowDirection left, ArrowDirection right1, ArrowDirection right2) {
		return new BiCommand<Arrow, Node>(new ArrowAccessor(left), new PtrPtrNodeAccessor(right1, right2),
				new SetOperation());
	}

	public static Command createSetCommandLLR(ArrowDirection left1, ArrowDirection left2, ArrowDirection right) {
		return new BiCommand<Arrow, Node>(new PtrArrowAccessor(left1, left2), new PtrNodeAccessor(right),
				new SetOperation());
	}

	public static Command createSetCommandLLRR(ArrowDirection left1, ArrowDirection left2, ArrowDirection right1, ArrowDirection right2) {
		return new BiCommand<Arrow, Node>(new PtrArrowAccessor(left1, left2), new PtrPtrNodeAccessor(right1, right2),
				new SetOperation());
	}

}

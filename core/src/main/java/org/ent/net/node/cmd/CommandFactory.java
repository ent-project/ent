package org.ent.net.node.cmd;

import org.ent.net.ArrowDirection;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.ArrowAccessor;
import org.ent.net.node.cmd.accessor.Level3Accessor;
import org.ent.net.node.cmd.accessor.NodeAccessor;
import org.ent.net.node.cmd.accessor.PtrArrowAccessor;
import org.ent.net.node.cmd.operation.AncestorExchangeOperation;
import org.ent.net.node.cmd.operation.AncestorExchangeNormalOperation;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.DupOperation;
import org.ent.net.node.cmd.operation.DupNormalOperation;
import org.ent.net.node.cmd.operation.IsIdenticalOperation;
import org.ent.net.node.cmd.operation.SetOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandFactory {

	private static final Map<Integer, Command> commandMap = new HashMap<>();

	static {
		initializeCommands();
	}

	private static final Map<String, Command> commandsByName = buildCommandsByName();

	private CommandFactory() {
	}


	private static void initializeCommands() {
		List<Accessor> accessors = new ArrayList<>();
		accessors.add(new NodeAccessor());
		for (ArrowDirection direction : ArrowDirection.values()) {
			accessors.add(new ArrowAccessor(direction));
		}
		for (ArrowDirection direction1 : ArrowDirection.values()) {
			for (ArrowDirection direction2 : ArrowDirection.values()) {
				accessors.add(new PtrArrowAccessor(direction1, direction2));
			}
		}
		for (ArrowDirection direction1 : ArrowDirection.values()) {
			for (ArrowDirection direction2 : ArrowDirection.values()) {
				for (ArrowDirection direction3 : ArrowDirection.values()) {
					accessors.add(new Level3Accessor(direction1, direction2, direction3));
				}
			}
		}
		initializeCommand(new NopCommand());
		List<BiOperation> operations = List.of(new SetOperation(), new AncestorExchangeOperation(), new AncestorExchangeNormalOperation(),
		new DupOperation(), new DupNormalOperation(), new IsIdenticalOperation());
		for (Accessor accessor1 : accessors) {
			for (Accessor accessor2 : accessors) {
				for (BiOperation operation : operations) {
					Command command = new BiCommand(accessor1, accessor2, operation);
					initializeCommand(command);
				}
			}
		}
	}

	private static void initializeCommand(Command command) {
		int value = command.getValue();
		if (commandMap.containsKey(value)) {
			throw new AssertionError();
		}
		commandMap.put(value, command);
	}

	private static Map<String, Command> buildCommandsByName() {
		Map<String, Command> result = new HashMap<>();
		for (Command command : commandMap.values()) {
			String name = command.getShortName();
			if (result.containsKey(name)) {
				throw new AssertionError("Duplicate command name: " + name);
			}
			result.put(name, command);
			String nameAscii = command.getShortNameAscii();
			if (!name.equals(nameAscii)) {
				if (result.containsKey(nameAscii)) {
					throw new AssertionError("Duplicate command name: " + nameAscii);
				}
				result.put(nameAscii, command);
			}
		}
		return result;
	}

	public static Command getByValue(int value) {
		return commandMap.get(value);
	}

	public static Command getByName(String name) {
		return commandsByName.get(name);
	}

	public static Command createNopCommand() {
		return new NopCommand();
	}

	public static Command createSetCommandL(ArrowDirection left) {
		return new BiCommand(new ArrowAccessor(left), new NodeAccessor(), new SetOperation());
	}

	public static Command createSetCommandLR(ArrowDirection left, ArrowDirection right) {
		return new BiCommand(new ArrowAccessor(left), new ArrowAccessor(right), new SetOperation());
	}

	public static Command createDupCommand(ArrowDirection left) {
		return new BiCommand(new ArrowAccessor(left), new NodeAccessor(), new DupOperation());
	}

	public static Command createAncestorSwapCommand() {
		return new BiCommand(new NodeAccessor(), new NodeAccessor(), new AncestorExchangeOperation());
	}

	public static Command createIsIdenticalCommand(ArrowDirection left, ArrowDirection right) {
		return new BiCommand(new ArrowAccessor(left), new ArrowAccessor(right), new IsIdenticalOperation());
	}
}

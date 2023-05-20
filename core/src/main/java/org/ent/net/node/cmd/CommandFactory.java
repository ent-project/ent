package org.ent.net.node.cmd;

import org.ent.net.ArrowDirection;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.accessor.DirectAccessor;
import org.ent.net.node.cmd.accessor.PrimaryAccessor;
import org.ent.net.node.cmd.operation.AncestorExchangeOperation;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.DupOperation;
import org.ent.net.node.cmd.operation.IsIdenticalOperation;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.SetOperation;
import org.ent.net.node.cmd.operation.TriOperation;

import java.util.Collection;
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
		initializeCommand(new NopCommand());
		List<BiOperation> biOperations = List.of(
				Operations.SET_OPERATION,
				Operations.ANCESTOR_EXCHANGE_OPERATION,
				Operations.ANCESTOR_EXCHANGE_NORMAL_OPERATION,
				Operations.DUP_OPERATION,
				Operations.DUP_NORMAL_OPERATION,
				Operations.IS_IDENTICAL_OPERATION,
				Operations.SET_VALUE_OPERATION,
				Operations.NEG_OPERATION,
				Operations.INC_OPERATION,
				Operations.DEC_OPERATION
		);
		for (Accessor accessor1 : Accessors.ALL_ACCESSORS) {
			for (Accessor accessor2 : Accessors.ALL_ACCESSORS) {
				for (BiOperation operation : biOperations) {
					Command command = new BiCommand(accessor1, accessor2, operation);
					initializeCommand(command);
				}
			}
		}
		List<TriOperation> triOperations = List.of(
				Operations.PLUS_OPERATION,
				Operations.MINUS_OPERATION,
				Operations.MULTIPLY_OPERATION,
				Operations.MODULO_OPERATION,
				Operations.XOR_OPERATION,
				Operations.BITWISE_AND_OPERATION,
				Operations.BITWISE_OR_OPERATION,
				Operations.ROTATE_RIGHT_OPERATION,
				Operations.SHIFT_LEFT_OPERATION,
				Operations.SHIFT_RIGHT_OPERATION
		);
		for (Accessor accessor1 : Accessors.ALL_ACCESSORS) {
			for (Accessor accessor2 : Accessors.ALL_ACCESSORS) {
				for (Accessor accessor3 : Accessors.ALL_ACCESSORS) {
					for (TriOperation operation : triOperations) {
						Command command = new TriCommand(accessor1, accessor2, accessor3, operation);
						initializeCommand(command);
					}
				}
			}
		}
	}

	private static void initializeCommand(Command command) {
		int value = command.getValue();
		if (commandMap.containsKey(value)) {
			throw new AssertionError("duplicate value: " + Integer.toBinaryString(value));
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
		}
		return result;
	}

	public static Collection<String> getAllCommandNames() {
		return commandsByName.keySet();
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
		return new BiCommand(new PrimaryAccessor(left), new DirectAccessor(), new SetOperation());
	}

	public static Command createSetCommandLR(ArrowDirection left, ArrowDirection right) {
		return new BiCommand(new PrimaryAccessor(left), new PrimaryAccessor(right), new SetOperation());
	}

	public static Command createDupCommand(ArrowDirection left) {
		return new BiCommand(new PrimaryAccessor(left), new DirectAccessor(), new DupOperation());
	}

	public static Command createAncestorSwapCommand() {
		return new BiCommand(new DirectAccessor(), new DirectAccessor(), new AncestorExchangeOperation());
	}

	public static Command createIsIdenticalCommand(ArrowDirection left, ArrowDirection right) {
		return new BiCommand(new PrimaryAccessor(left), new PrimaryAccessor(right), new IsIdenticalOperation());
	}
}

package org.ent.net.node.cmd;

import org.ent.net.ArrowDirection;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.DirectAccessor;
import org.ent.net.node.cmd.accessor.PrimaryAccessor;
import org.ent.net.node.cmd.accessor.SecondaryAccessor;
import org.ent.net.node.cmd.accessor.TertiaryAccessor;
import org.ent.net.node.cmd.operation.AncestorExchangeNormalOperation;
import org.ent.net.node.cmd.operation.AncestorExchangeOperation;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.DupNormalOperation;
import org.ent.net.node.cmd.operation.DupOperation;
import org.ent.net.node.cmd.operation.IsIdenticalOperation;
import org.ent.net.node.cmd.operation.SetOperation;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.node.cmd.operation.math.BitwiseAndOperation;
import org.ent.net.node.cmd.operation.math.BitwiseOrOperation;
import org.ent.net.node.cmd.operation.math.MinusOperation;
import org.ent.net.node.cmd.operation.math.ModuloOperation;
import org.ent.net.node.cmd.operation.math.MultiplyOperation;
import org.ent.net.node.cmd.operation.math.PlusOperation;
import org.ent.net.node.cmd.operation.math.RotateRightOperation;
import org.ent.net.node.cmd.operation.math.ShiftLeftOperation;
import org.ent.net.node.cmd.operation.math.ShiftRightOperation;
import org.ent.net.node.cmd.operation.math.XorOperation;

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
		accessors.add(new DirectAccessor());
		for (ArrowDirection direction : ArrowDirection.values()) {
			accessors.add(new PrimaryAccessor(direction));
		}
		for (ArrowDirection direction1 : ArrowDirection.values()) {
			for (ArrowDirection direction2 : ArrowDirection.values()) {
				accessors.add(new SecondaryAccessor(direction1, direction2));
			}
		}
		for (ArrowDirection direction1 : ArrowDirection.values()) {
			for (ArrowDirection direction2 : ArrowDirection.values()) {
				for (ArrowDirection direction3 : ArrowDirection.values()) {
					accessors.add(new TertiaryAccessor(direction1, direction2, direction3));
				}
			}
		}
		initializeCommand(new NopCommand());
		List<BiOperation> biOperations = List.of(
				new SetOperation(),
				new AncestorExchangeOperation(),
				new AncestorExchangeNormalOperation(),
				new DupOperation(),
				new DupNormalOperation(),
				new IsIdenticalOperation()
		);
		for (Accessor accessor1 : accessors) {
			for (Accessor accessor2 : accessors) {
				for (BiOperation operation : biOperations) {
					Command command = new BiCommand(accessor1, accessor2, operation);
					initializeCommand(command);
				}
			}
		}
		List<TriOperation> triOperations = List.of(
				new PlusOperation(),
				new MinusOperation(),
				new MultiplyOperation(),
				new ModuloOperation(),
				new XorOperation(),
				new BitwiseAndOperation(),
				new BitwiseOrOperation(),
				new RotateRightOperation(),
				new ShiftLeftOperation(),
				new ShiftRightOperation()
		);
		for (Accessor accessor1 : accessors) {
			for (Accessor accessor2 : accessors) {
				for (Accessor accessor3 : accessors) {
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

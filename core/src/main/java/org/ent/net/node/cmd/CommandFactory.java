package org.ent.net.node.cmd;

import org.ent.net.ArrowDirection;
import org.ent.net.node.cmd.accessor.ArrowAccessor;
import org.ent.net.node.cmd.accessor.NodeAccessor;
import org.ent.net.node.cmd.accessor.PtrArrowAccessor;
import org.ent.net.node.cmd.accessor.PtrNodeAccessor;
import org.ent.net.node.cmd.accessor.PtrPtrNodeAccessor;
import org.ent.net.node.cmd.operation.AncestorSwapOperation;
import org.ent.net.node.cmd.operation.DupOperation;
import org.ent.net.node.cmd.operation.EvalOperation;
import org.ent.net.node.cmd.operation.IsIdenticalOperation;
import org.ent.net.node.cmd.operation.SetOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandFactory {

	public static final Command NOP_COMMAND = createNopCommand();
	public static final Command ANCESTOR_SWAP_COMMAND = createAncestorSwapCommand();

	private static final int MAX_EVAL_LEVEL = 5;

	private static final List<Command> allCommands;


	static {
		List<Command> result = new ArrayList<>();
		result.add(NOP_COMMAND);
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
		for (ArrowDirection left : ArrowDirection.values()) {
			result.add(createDupCommand(left));
		}
		result.add(ANCESTOR_SWAP_COMMAND);
		for (ArrowDirection left : ArrowDirection.values()) {
			for (ArrowDirection right : ArrowDirection.values()) {
				result.add(createAncestorSwapCommandLR(left, right));
			}
		}
		for (int evalLevel = 1; evalLevel <= MAX_EVAL_LEVEL; evalLevel++) {
			result.add(createEvalCommand(evalLevel));
			for (ArrowDirection left : ArrowDirection.values()) {
				for (ArrowDirection right : ArrowDirection.values()) {
					result.add(createEvalCommandLR(evalLevel, left, right));
				}
			}
		}
		for (ArrowDirection left : ArrowDirection.values()) {
			for (ArrowDirection right : ArrowDirection.values()) {
				result.add(createIsIdenticalCommand(left, right));
			}
		}
		allCommands = result;
	}

	private static final Map<String, Command> commandsByName = buildCommandsByName();

	private CommandFactory() {
	}

	private static Map<String, Command> buildCommandsByName() {
		Map<String, Command> result = new HashMap<>();
		for (Command command : allCommands) {
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

	public static Command getByName(String name) {
		return commandsByName.get(name);
	}

	public static Command createNopCommand() {
		return new NopCommand();
	}

	public static Command createSetCommandL(ArrowDirection left) {
		return new BiCommand<>(new ArrowAccessor(left), new NodeAccessor(), new SetOperation());
	}

	public static Command createSetCommandLL(ArrowDirection left1, ArrowDirection left2) {
		return new BiCommand<>(new PtrArrowAccessor(left1, left2), new NodeAccessor(), new SetOperation());
	}

	public static Command createSetCommandLR(ArrowDirection left, ArrowDirection right) {
		return new BiCommand<>(new ArrowAccessor(left), new PtrNodeAccessor(right), new SetOperation());
	}

	public static Command createSetCommandLRR(ArrowDirection left, ArrowDirection right1, ArrowDirection right2) {
		return new BiCommand<>(new ArrowAccessor(left), new PtrPtrNodeAccessor(right1, right2),
				new SetOperation());
	}

	public static Command createSetCommandLLR(ArrowDirection left1, ArrowDirection left2, ArrowDirection right) {
		return new BiCommand<>(new PtrArrowAccessor(left1, left2), new PtrNodeAccessor(right),
				new SetOperation());
	}

	public static Command createSetCommandLLRR(ArrowDirection left1, ArrowDirection left2, ArrowDirection right1, ArrowDirection right2) {
		return new BiCommand<>(new PtrArrowAccessor(left1, left2), new PtrPtrNodeAccessor(right1, right2),
				new SetOperation());
	}

	public static Command createDupCommand(ArrowDirection left) {
		return new BiCommand<>(new ArrowAccessor(left), new NodeAccessor(), new DupOperation());
	}

	public static Command createAncestorSwapCommand() {
		return new BiCommand<>(new NodeAccessor(), new NodeAccessor(), new AncestorSwapOperation());
	}

	public static Command createAncestorSwapCommandLR(ArrowDirection left, ArrowDirection right) {
		return new BiCommand<>(new PtrNodeAccessor(left), new PtrNodeAccessor(right), new AncestorSwapOperation());
	}

	public static Command createEvalCommand(int evalLevel) {
		return new BiCommand<>(new NodeAccessor(), new NodeAccessor(), new EvalOperation(evalLevel));
	}

	public static Command createEvalCommandLR(int evalLevel, ArrowDirection left, ArrowDirection right) {
		return new BiCommand<>(new PtrNodeAccessor(left), new PtrNodeAccessor(right), new EvalOperation(evalLevel));
	}

	public static Command createIsIdenticalCommand(ArrowDirection left, ArrowDirection right) {
		return new BiCommand<>(new ArrowAccessor(left), new ArrowAccessor(right), new IsIdenticalOperation());
	}

}

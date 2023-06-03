package org.ent.net.node.cmd;

import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriOperation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands {

    private static final Map<Integer, Command> commandMap = initializeCommandMap();
    private static final Map<String, Command> commandsByName = buildCommandsByName();

    public static final Command NOP = getByValue(new NopCommand().getValue());
    public static final Command ANCESTOR_EXCHANGE = get(Operations.ANCESTOR_EXCHANGE_OPERATION);
    public static final Command SET = get(Operations.SET_OPERATION);

    private Commands() {
    }

    public static Command get(BiOperation operation, Accessor accessor1, Accessor accessor2) {
        BiCommand command = new BiCommand(operation, accessor1, accessor2);
        return getByValue(command.getValue());
    }

    public static Command get(BiOperation operation) {
        BiCommand command = new BiCommand(operation, Accessors.LEFT, Accessors.RIGHT);
        return getByValue(command.getValue());
    }

    public static Command get(TriOperation operation, Accessor accessor1, Accessor accessor2, Accessor accessor3) {
        TriCommand command = new TriCommand(operation, accessor1, accessor2, accessor3);
        return getByValue(command.getValue());
    }

    static  Map<Integer, Command> initializeCommandMap() {
        HashMap<Integer, Command> result = new HashMap<>();
        initializeCommand(new NopCommand(), result);
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
                    Command command = new BiCommand(operation, accessor1, accessor2);
                    initializeCommand(command, result);
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
                        Command command = new TriCommand(operation, accessor1, accessor2, accessor3);
                        initializeCommand(command, result);
                    }
                }
            }
        }
        return result;
    }

    private static void initializeCommand(Command command, HashMap<Integer, Command> map) {
        int value = command.getValue();
        if (map.containsKey(value)) {
            throw new AssertionError("duplicate value: " + Integer.toBinaryString(value));
        }
        map.put(value, command);
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
}

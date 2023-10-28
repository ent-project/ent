package org.ent.util.builder;

import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.veto.BiCondition;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

import java.util.function.Consumer;

public final class NodeTemplate implements NodeProxy {
    private int value;
    private NodeProxy left;
    private NodeProxy right;
    private boolean root;
    private String name;
    private Node resolved;

    public static NodeTemplate node() {
        return new NodeTemplate();
    }

    public static class CommandInfo {
        private BiOperation biOperation;
        private Integer numArgs;
        private NodeProxy arg1;
        private NodeProxy arg2;
        private Accessor accessor1;
        private Accessor accessor2;

        public CommandInfo operation(BiOperation biOperation) {
            this.biOperation = biOperation;
            return this;
        }

        public CommandInfo argument1(NodeProxy singleArg, ArgSingle argSingle) {
            this.arg1 = singleArg;
            this.numArgs = 1;
            this.accessor1 = argSingle.accessor();
            return this;
        }

        public CommandInfo argument1(NodeProxy singleArg, Arg1 arg1) {
            this.arg1 = singleArg;
            this.accessor1 = arg1.accessor();
            return this;
        }

        public CommandInfo argument2(NodeProxy singleArg, ArgSingle argSingle) {
            this.arg2 = singleArg;
            this.numArgs = 1;
            this.accessor2 = argSingle.accessor();
            return this;
        }

        public CommandInfo argument2(NodeProxy singleArg, Arg2 arg2) {
            this.arg2 = singleArg;
            this.accessor2 = arg2.accessor();
            return this;
        }
    }

    public Node get() {
        return resolved;
    }

    public NodeTemplate setRoot() {
        this.root = true;
        return this;
    }

    public boolean isRoot() {
        return root;
    }

    public NodeTemplate name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public NodeTemplate value(int value) {
        this.value = value;
        return this;
    }

    public int getValue() {
        return value;
    }

    public void setResolved(Node resolved) {
        this.resolved = resolved;
    }

    public Node getResolved() {
        return resolved;
    }

    public NodeProxy getLeft() {
        return left;
    }

    public NodeProxy getRight() {
        return right;
    }

    public NodeTemplate command(Command command) {
        this.value = command.getValue();
        return this;
    }

    public NodeTemplate command(BiOperation operation, Accessor accessor1, Accessor accessor2) {
        Command command = Commands.get(operation, accessor1, accessor2);
        this.value = command.getValue();
        return this;
    }

    public NodeTemplate branching(BiOperation operation, Accessor accessor1, Accessor accessor2a, Accessor accessor2b) {
        Command command = Commands.getBranching(operation, accessor1, accessor2a, accessor2b);
        this.value = command.getValue();
        return this;
    }


    public NodeTemplate command(Consumer<CommandInfo> consumer) {
        CommandInfo info = new CommandInfo();
        consumer.accept(info);
        if (info.numArgs != null && info.numArgs == 1) {
            this.command(info.biOperation, info.accessor1, info.accessor2);
            this.left(info.arg1);
        } else {
            this.command(info.biOperation, info.accessor1, info.accessor2);
            this.left(node()
                    .left(info.arg1)
                    .right(info.arg2));
        }
        return this;
    }

    public NodeTemplate veto(BiCondition condition, Accessor accessor1, Accessor accessor2) {
        Veto veto = Vetos.get(condition, accessor1, accessor2);
        this.value = veto.getValue();
        return this;
    }

    public NodeTemplate left(NodeProxy left) {
        this.left = left;
        return this;
    }

    public NodeTemplate right(NodeProxy right) {
        this.right = right;
        return this;
    }
}

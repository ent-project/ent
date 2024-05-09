package org.ent.util.builder;

import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.node.cmd.split.BiCondition;
import org.ent.net.node.cmd.split.Split;
import org.ent.net.node.cmd.split.Splits;

import java.util.function.Consumer;

public final class NodeTemplate implements NodeProxy {

    public static final boolean NOT = true;

    private boolean split;
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
        private TriOperation triOperation;
        private Integer numArgs;
        private NodeProxy arg1;
        private NodeProxy arg2;
        private NodeProxy arg3;
        private Accessor accessor1;
        private Accessor accessor2;
        private Accessor accessor3;

        public CommandInfo operation(BiOperation biOperation) {
            this.biOperation = biOperation;
            return this;
        }

        public CommandInfo operation(TriOperation triOperation) {
            this.triOperation = triOperation;
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

        public CommandInfo argument1(Accessor accessor, NodeProxy singleArg) {
            this.arg1 = singleArg;
            this.accessor1 = accessor;
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

        public CommandInfo argument2(Accessor accessor, NodeProxy singleArg) {
            this.arg2 = singleArg;
            this.accessor2 = accessor;
            return this;
        }

        public CommandInfo argument3(Accessor accessor, NodeProxy singleArg) {
            this.arg3 = singleArg;
            this.accessor3 = accessor;
            return this;
        }

    }

    public Node get() {
        return resolved;
    }

    public boolean isSplit() {
        return split;
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


    public NodeTemplate command(BiOperation operation, NodeProxy n1, NodeProxy n2) {
        if (n1 == n2) {
            throw new UnsupportedOperationException("combination not supported yet");
        }
        Command command = Commands.get(operation, Accessors.LL, Accessors.LR);
        this.value(command.getValue());
        this.left(node()
                .left(n1)
                .right(n2));
        return this;
    }

    public NodeTemplate command(TriOperation operation, Accessor accessor1, Accessor accessor2, Accessor accessor3) {
        Command command = Commands.get(operation, accessor1, accessor2, accessor3);
        this.value = command.getValue();
        return this;
    }

    public NodeTemplate command(TriOperation operation, NodeProxy n1, NodeProxy n2, NodeProxy n3) {
        Accessor accessor1;
        Accessor accessor2;
        Accessor accessor3;
        NodeProxy arg1 = null;
        NodeProxy arg2 = null;
        if (n1 == n2) {
            accessor1 = Accessors.LL;
            accessor2 = Accessors.LL;
            accessor3 = Accessors.LR;
            arg1 = n1;
            arg2 = n3;
        } else if (n2 != n3 && n3 != n1) {
            accessor1 = Accessors.LL;
            accessor2 = Accessors.LRL;
            accessor3 = Accessors.LRR;
        } else {
            throw new UnsupportedOperationException("combination not supported yet");
        }
        Command command = Commands.get(operation, accessor1, accessor2, accessor3);
        this.value(command.getValue());
        if (arg1 != null) {
            this.left(node()
                    .left(arg1)
                    .right(arg2));
        } else {
            this.left(node()
                    .left(n1)
                    .right(node()
                            .left(n2)
                            .right(n3)));
        }
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
            if (info.biOperation != null) {
                this.command(info.biOperation, info.accessor1, info.accessor2);
                this.left(node()
                        .left(info.arg1)
                        .right(info.arg2));
            } else {
                this.command(info.triOperation, info.accessor1, info.accessor2, info.accessor3);
                insertParameter(info.accessor1, info.arg1);
                insertParameter(info.accessor2, info.arg2);
                insertParameter(info.accessor3, info.arg3);
            }
        }
        return this;
    }

    private void insertParameter(Accessor accessor, NodeProxy arg) {
        ArrowDirection[] path = accessor.getPath();
        NodeTemplate cur = this;
        for (int i = 0; i < path.length; i++) {
            ArrowDirection arrowDirection = path[i];
            switch (arrowDirection) {
                case LEFT -> {
                    if (cur.getLeft() == null) {
                        if (i == path.length - 1) {
                            cur.left(arg);
                        } else {
                            cur.left(node());
                        }
                    }
                    cur = (NodeTemplate) cur.getLeft();
                }
                case RIGHT -> {
                    if (cur.getRight() == null) {
                        if (i == path.length - 1) {
                            cur.right(arg);
                        } else {
                            cur.right(node());
                        }
                    }
                    cur = (NodeTemplate) cur.getRight();
                }
            }
        }
    }

    public NodeTemplate split(BiCondition condition, boolean not, Accessor accessor1, Accessor accessor2) {
        Split split = Splits.get(condition, not, accessor1, accessor2);
        this.value = split.getValue();
        this.split = true;
        return this;
    }

    public NodeTemplate split(BiCondition condition, Accessor accessor1, Accessor accessor2) {
        return split(condition, false, accessor1, accessor2);
    }

    public NodeTemplate split(BiCondition condition, boolean not, NodeProxy n1, NodeProxy n2) {
        if (n1 == n2) {
            throw new UnsupportedOperationException("combination not supported yet");
        }
        Split split = Splits.get(condition, not, Accessors.LL, Accessors.LR);
        this.value(split.getValue());
        this.left(node()
                .left(n1)
                .right(n2));
        return this;
    }

    public NodeTemplate split(BiCondition condition,  NodeProxy n1, NodeProxy n2) {
        return split(condition, false, n1, n2);
    }

    public NodeTemplate left(NodeProxy left) {
        if (this.left != null) {
            throw new IllegalArgumentException("left already set");
        }
        this.left = left;
        return this;
    }

    public NodeTemplate right(NodeProxy right) {
        if (this.right != null) {
            throw new IllegalArgumentException("right already set");
        }
        this.right = right;
        return this;
    }
}

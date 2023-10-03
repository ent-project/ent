package org.ent.net;

import org.ent.Ent;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;
import org.ent.permission.WriteFacet;

import static org.ent.net.node.cmd.accessor.Accessors.*;
import static org.ent.util.NetBuilder.*;

public class CopyValueGameTestSetup {

    public Ent ent;

    public Net verifier;
    public final int targetValue = 7;

    public Net input;
    public Node inputNode;

    public void build() {
        input = builder().net(inputNode = value(0));

        Node data;
        verifier = builder().net(
                node(Commands.NOP,
                        data = node(input.getRoot(), value(targetValue)),
                        node(Commands.get(Operations.SET_OPERATION, R, LR),
                                node(Vetos.get(Conditions.SAME_VALUE_CONDITION, LLL, LLR),
                                        data,
                                        value(Commands.CONCLUSION_SUCCESS)),
                                value(Commands.CONCLUSION_FAILURE))));

        Node toVerifier;
        ent = builder().ent(
                node(Commands.get(Operations.SET_VALUE_OPERATION, LLLL, LLLR),
                        toVerifier = unary(verifier.getRoot()),
                        node(Commands.get(Operations.EVAL_FLOW_OPERATION, LL),
                                toVerifier,
                                node(Commands.get(Operations.EVAL_FLOW_OPERATION, LL),
                                        toVerifier,
                                        value(Commands.CONCLUSION_SUCCESS)))));
        ent.addDomain(input);
        ent.addDomain(verifier);
        input.setName("input");
        verifier.setName("verifier");
        verifier.setName(data, "data");
        input.setName(inputNode, "x");

        ent.putPermissions(p -> p.net(np -> np
                .canExecute(verifier)
                .canWrite(input, WriteFacet.VALUE)));
    }
}

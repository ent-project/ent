package org.ent.dev.plan;

import org.ent.net.node.CNode;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.ExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VariabilityCollectorTest {

    private static final CNode cNodeNop = new CNode(CommandFactory.createNopCommand());
    private static final CNode cNodeIx = new CNode(CommandFactory.createAncestorSwapCommand());

    @Test
    void test() {
        VariabilityCollector collector = new VariabilityCollector();
        for (CNode cNode : List.of(cNodeNop, cNodeIx, cNodeNop)) {
            collector.fireCommandExecuted(cNode, ExecutionResult.NORMAL);
        }

        Map<Command, VariabilityCollector.CommandData> commandDataMap = collector.commandDataMap;
        assertThat(commandDataMap.get(cNodeNop.getCommand()).getTimesExecuted()).isEqualTo(2);
        assertThat(commandDataMap.get(cNodeIx.getCommand()).getTimesExecuted()).isEqualTo(1);
    }
}
package org.ent.dev.game.juniper;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.ent.dev.game.juniper.HabitatMap.MAP_TINY;

class JuniperGameTest {

    @Test
    void locatorAutomaton() {
        HabitatMap map = new HabitatMap(MAP_TINY);
        JuniperGame game = new JuniperGame(map);
        Ent ent = game.getEnt();

        game.buildHabitat();

        Net locationSetterNet = game.buildLocationSetterNet();

        Field field = map.getField(1, 0);
        Net locatorNet = game.buildLocatorMechanism(field.node(), locationSetterNet.getRoot());
        ent.addDomain(locatorNet);

        Node root = ent.getNet().getRoot();
        root.setValue(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.L).getValue(), Permissions.DIRECT);
        root.setLeftChild(locatorNet.getRoot(), Permissions.DIRECT);

        ent.putPermissions(pb -> pb
                .net(p -> p
                        .canExecute(locatorNet)
                        .canWrite(locationSetterNet, WriteFacet.ARROW))
                .domain(locatorNet, p -> p
                        .canPointTo(game.getHabitat())
                        .canPointTo(locationSetterNet)
                        .canWrite(locationSetterNet, WriteFacet.ARROW))
                .domain(locationSetterNet, p -> p
                        .canPointTo(locatorNet))
        );

        StringBuilder sb = new StringBuilder();
        NetFormatter formatter = new NetFormatter().withForceGivenNodeNames(true);

        EntRunner runner = new EntRunner(ent);
        for (int i = 1; i < 30; i++) {
            sb.append(formatter.format(ent)).append("\n");
            StepResult stepResult = runner.step();
            sb.append("step result %s: %s\n".formatted(i, stepResult));
            if (stepResult == StepResult.CONCLUDED) {
                break;
            }
            sb.append("\n");
        }

        assertThat(normalize(sb.toString())).isEqualTo("""
                <eval_flow/>[A]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { A:<///::/\\//>((i:#0, location:(f_1_0, a)), is_found:<?////===/\\\\/?>((i, location), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A)), is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i:<//:://\\>(i, is_found)))))) }
                step result 1: SUCCESS

                <eval_flow/>[is_found]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { is_found:<?////===/\\\\/?>((i:[C], location:(f_1_0, a)), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A:<///::/\\//>((i, location), is_found))), is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i:<//:://\\>(i, is_found))))) }
                step result 2: SUCCESS

                <eval_flow/>[is_end_of_list]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { is_end_of_list:<?//\\===//?>(i:[C], (exit_failure:<FAILURE>(exit_failure, A:<///::/\\//>((i, location:(f_1_0, a)), is_found:<?////===/\\\\/?>((i, location), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A)), is_end_of_list)))), inc_i:<//:://\\>(i, is_found))) }
                step result 3: SUCCESS

                <eval_flow/>[inc_i]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { inc_i:<//:://\\>(i:[C], is_found:<?////===/\\\\/?>((i, location:(f_1_0, a)), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A:<///::/\\//>((i, location), is_found))), is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i))))) }
                step result 4: SUCCESS

                <eval_flow/>[is_found]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { is_found:<?////===/\\\\/?>((i:[D], location:(f_1_0, a)), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A:<///::/\\//>((i, location), is_found))), is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i:<//:://\\>(i, is_found))))) }
                step result 5: SUCCESS

                <eval_flow/>[is_end_of_list]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { is_end_of_list:<?//\\===//?>(i:[D], (exit_failure:<FAILURE>(exit_failure, A:<///::/\\//>((i, location:(f_1_0, a)), is_found:<?////===/\\\\/?>((i, location), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A)), is_end_of_list)))), inc_i:<//:://\\>(i, is_found))) }
                step result 6: SUCCESS

                <eval_flow/>[inc_i]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { inc_i:<//:://\\>(i:[D], is_found:<?////===/\\\\/?>((i, location:(f_1_0, a)), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A:<///::/\\//>((i, location), is_found))), is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i))))) }
                step result 7: SUCCESS

                <eval_flow/>[is_found]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { is_found:<?////===/\\\\/?>((i:[b], location:(f_1_0, a)), (set_new_location:<///::/\\/>((location, a), B:<SUCCESS>(B, A:<///::/\\//>((i, location), is_found))), is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i:<//:://\\>(i, is_found))))) }
                step result 8: SUCCESS

                <eval_flow/>[set_new_location]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { set_new_location:<///::/\\/>((location:(f_1_0, a), a), B:<SUCCESS>(B, A:<///::/\\//>((i:[b], location), is_found:<?////===/\\\\/?>((i, location), (set_new_location, is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i:<//:://\\>(i, is_found)))))))) }
                step result 9: SUCCESS

                <eval_flow/>[B]
                habitat { ... }
                location_setter { a:[f_0_0] }
                locator { B:<SUCCESS>(B, A:<///::/\\//>((i:[b], location:(f_0_0, a)), is_found:<?////===/\\\\/?>((i, location), (set_new_location:<///::/\\/>((location, a), B), is_end_of_list:<?//\\===//?>(i, (exit_failure:<FAILURE>(exit_failure, A), inc_i:<//:://\\>(i, is_found))))))) }
                step result 10: CONCLUDED
                """);
    }

    private String normalize(String s) {
        return s.replaceAll("habitat \\{.*\\}", "habitat { ... }");
    }
}
package org.ent.net.node.cmd;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.accessor.DirectAccessor;
import org.ent.net.node.cmd.accessor.TertiaryAccessor;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.SetOperation;
import org.ent.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.net.node.cmd.operation.Operations.INC_OPERATION;
import static org.ent.net.node.cmd.veto.Conditions.GREATER_THAN_CONDITION;
import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;
import static org.ent.util.NetBuilder.value;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BiCommandTest {

	@Mock
	private Accessor accessor1;

	@Mock
	private Accessor accessor2;

	@Mock
	private BiOperation operation;

	@Test
	void getShortName() {
		when(accessor1.getShortName()).thenReturn("a");
		when(operation.getShortName()).thenReturn("x");
		when(accessor2.getShortName()).thenReturn("b");
		BiCommand command = new BiCommand(operation, accessor1, accessor2);

		assertThat(command.getShortName()).isEqualTo("axb");

		verify(accessor1).getShortName();
		verify(operation).getShortName();
		verify(accessor2).getShortName();
	}

	@Test
	void getValue() {
		Accessor a1 = new TertiaryAccessor(ArrowDirection.RIGHT, ArrowDirection.RIGHT, ArrowDirection.RIGHT);
		Accessor a2 = new DirectAccessor();
		BiCommand c = new BiCommand(new SetOperation(), a1, a2);
		assertThat(TestUtil.toBinary16bit(c.getValue())).isEqualTo(
				TestUtil.toBinary16bit(0b0011_0011__0000_0001__1111_0000__0000_0001));
	}

	@Test
	void execute_withVeto_pass() {
		Node i;
		Net net = builder().net(unary(node(GREATER_THAN_CONDITION, i = value(7), value(0))));
		Command command = new MonoCommand(INC_OPERATION, Accessors.LEFT);

		command.execute(net.getRoot(), net.getPermissions());

		assertThat(i.getValue()).isEqualTo(8);
	}

	@Test
	void execute_withVeto_reject() {
		Node i;
		Net net = builder().net(unary(node(GREATER_THAN_CONDITION, i = value(7), value(1000))));
		Command command = new MonoCommand(INC_OPERATION, Accessors.LEFT);

		command.execute(net.getRoot(), net.getPermissions());

		assertThat(i.getValue()).isEqualTo(7);
	}

}

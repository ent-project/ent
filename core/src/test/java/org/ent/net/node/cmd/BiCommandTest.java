package org.ent.net.node.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.Net;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.BiOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BiCommandTest {

	private BiCommand<Arrow, Node> command;

	@Mock
	private Accessor<Arrow> accessor1;

	@Mock
	private Accessor<Node> accessor2;

	@Mock
	private BiOperation<Arrow, Node> operation;

	@BeforeEach
	void setUp() {
		command = new BiCommand<>(accessor1, accessor2, operation);
	}

	@Test
	void execute_error_noBNode() throws Exception {
		Net net = new NetParser().parse("A=[A]");

		assertThat(command.execute(net.getRoot())).isEqualTo(ExecutionResult.ERROR);

		verifyNoMoreInteractions(accessor1, accessor2, operation);
	}

	@Test
	void execute_error_accessor1Empty() throws Exception {
		Net net = new NetParser().parse("A=(A,A)");
		Node node = net.getRoot();

		assertThat(command.execute(node)).isEqualTo(ExecutionResult.ERROR);

		verify(accessor1).get(node, Purview.COMMAND);
		verifyNoMoreInteractions(accessor1, accessor2, operation);
	}

	@Test
	void execute_error_accessor2Empty() throws Exception {
		Net net = new NetParser().parse("A=(A,A)");
		Node node = net.getRoot();

		when(accessor1.get(node, Purview.COMMAND)).thenReturn(Optional.of(node.getArrow(ArrowDirection.LEFT)));

		assertThat(command.execute(node)).isEqualTo(ExecutionResult.ERROR);

		verify(accessor1).get(node, Purview.COMMAND);
		verify(accessor2).get(node, Purview.COMMAND);
		verifyNoMoreInteractions(accessor1, accessor2, operation);
	}

	@Test
	void execute_okay() throws Exception {
		Net net = new NetParser().parse("A=(A,A)");
		Node node = net.getRoot();
		Arrow arrow = node.getArrow(ArrowDirection.LEFT);

		when(accessor1.get(node, Purview.COMMAND)).thenReturn(Optional.of(arrow));
		when(accessor2.get(node, Purview.COMMAND)).thenReturn(Optional.of(node));
		when(operation.apply(any(), any())).thenReturn(ExecutionResult.NORMAL);

		assertThat(command.execute(node)).isEqualTo(ExecutionResult.NORMAL);

		verify(accessor1).get(node, Purview.COMMAND);
		verify(accessor2).get(node, Purview.COMMAND);
		verify(operation).apply(arrow, node);
		verifyNoMoreInteractions(accessor1, accessor2, operation);
	}

	@Test
	void getShortName() throws Exception {
		when(accessor1.getShortName()).thenReturn("a");
		when(operation.getShortName()).thenReturn("x");
		when(accessor2.getShortName()).thenReturn("b");

		assertThat(command.getShortName()).isEqualTo("axb");

		verify(accessor1).getShortName();
		verify(operation).getShortName();
		verify(accessor2).getShortName();
		verifyNoMoreInteractions(accessor1, accessor2, operation);
	}

}

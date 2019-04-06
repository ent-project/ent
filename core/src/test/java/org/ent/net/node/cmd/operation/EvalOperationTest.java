package org.ent.net.node.cmd.operation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class EvalOperationTest {

	@Test
	public void toSuperscriptNumber() throws Exception {
		assertThat(EvalOperation.toSuperscriptNumber(0)).isEqualTo("⁰");
		assertThat(EvalOperation.toSuperscriptNumber(1)).isEqualTo("¹");
		assertThat(EvalOperation.toSuperscriptNumber(9)).isEqualTo("⁹");
		assertThat(EvalOperation.toSuperscriptNumber(26035)).isEqualTo("²⁶⁰³⁵");
	}

}

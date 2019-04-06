package org.ent.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToolsTest {

	@Test
	public void binarySearchLowerEqual_nonStrict() throws Exception {
		double[] a = { 0.5, 3.0, 7.5 };

		assertThat(Tools.binarySearchLowerEqual(a, -1.0, false)).isEqualTo(0);
		assertThat(Tools.binarySearchLowerEqual(a, 0.0, false)).isEqualTo(0);
		assertThat(Tools.binarySearchLowerEqual(a, 0.4, false)).isEqualTo(0);
		assertThat(Tools.binarySearchLowerEqual(a, 0.5, false)).isEqualTo(0);
		assertThat(Tools.binarySearchLowerEqual(a, 0.6, false)).isEqualTo(1);
		assertThat(Tools.binarySearchLowerEqual(a, 2.9, false)).isEqualTo(1);
		assertThat(Tools.binarySearchLowerEqual(a, 3.0, false)).isEqualTo(1);
		assertThat(Tools.binarySearchLowerEqual(a, 3.1, false)).isEqualTo(2);
		assertThat(Tools.binarySearchLowerEqual(a, 7.4, false)).isEqualTo(2);
		assertThat(Tools.binarySearchLowerEqual(a, 7.5, false)).isEqualTo(2);
		assertThat(Tools.binarySearchLowerEqual(a, 7.6, false)).isEqualTo(2);
	}

	@Test
	public void binarySearchLowerEqual_strict() throws Exception {
		double[] a = { 0.5, 3.0, 7.5 };

		assertThat(Tools.binarySearchLowerEqual(a, 7.4, true)).isEqualTo(2);
		assertThat(Tools.binarySearchLowerEqual(a, 7.5, true)).isEqualTo(2);
		Assertions.assertThatThrownBy(() -> Tools.binarySearchLowerEqual(a, 7.6, true))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Input value x=7.6 is greater than the last value (a[2]=7.5)");
	}
}

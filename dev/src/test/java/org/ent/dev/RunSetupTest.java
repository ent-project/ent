package org.ent.dev;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class RunSetupTest {

	@Test
	public void equalsContract() {
	    EqualsVerifier.forClass(RunSetup.class).verify();
	}

}

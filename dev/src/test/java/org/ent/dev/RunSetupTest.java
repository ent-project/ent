package org.ent.dev;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class RunSetupTest {

	@Test
	void equalsContract() {
	    EqualsVerifier.forClass(RunSetup.class).verify();
	}

}

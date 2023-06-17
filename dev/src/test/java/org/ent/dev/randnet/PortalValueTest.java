package org.ent.dev.randnet;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalValueTest {

    @Test
    void create() {
        PortalValue portalValue = new PortalValue(0, 1);

        assertThat(portalValue.getValueBase()).isEqualTo(0xFFFEFFFF);
    }

}
package org.ent.dev.randnet;

import org.ent.net.node.cmd.Values;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultValueDrawingTest {
    @Test
    void drawValue() {
        DefaultValueDrawing defaultValueDrawing = new DefaultValueDrawing();
        for (int i = 0; i < 10000; i++) {
            int v = defaultValueDrawing.drawValue();
            String name = Values.getName(v);
            assertThat(name).isNotNull();
        }
    }

}
package org.ent.dev.randnet;

import org.ent.net.node.cmd.Values;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultValueDrawingTest {
    @Test
    void drawValue() {
        DefaultValueDrawing defaultValueDrawing = new DefaultValueDrawing();
        Random rand = new Random();
        for (int i = 0; i < 10000; i++) {
            int v = defaultValueDrawing.drawValue(rand);
            String name = Values.getName(v);
            assertThat(name).isNotNull();
        }
    }

}
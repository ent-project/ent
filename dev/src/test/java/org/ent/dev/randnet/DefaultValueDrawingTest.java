package org.ent.dev.randnet;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.net.node.cmd.Values;
import org.ent.net.util.RandomUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultValueDrawingTest {
    @Test
    void drawValue() {
        DefaultValueDrawing defaultValueDrawing = new DefaultValueDrawing();
        UniformRandomProvider rand = RandomUtil.newRandom2(0x7aL);
        for (int i = 0; i < 10000; i++) {
            int v = defaultValueDrawing.drawValue(rand);
            String name = Values.getName(v);
            assertThat(name).isNotNull();
        }
    }

}
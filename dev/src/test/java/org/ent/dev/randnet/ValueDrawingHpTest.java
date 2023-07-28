package org.ent.dev.randnet;

import org.ent.hyper.CollectingHyperManager;
import org.ent.hyper.HyperDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

class ValueDrawingHpTest {

    @Test
    void collectHyperparameters() {
        CollectingHyperManager hyperManager = new CollectingHyperManager();

        new ValueDrawingHp(hyperManager);

        List<HyperDefinition<?>> hyperDefinitions = hyperManager.getHyperDefinitions();
        System.err.println(hyperDefinitions);
    }

}
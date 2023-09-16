package org.ent.dev.hyper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteHyperManagerTest {

    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final IntHyperDefinition HYPER_MAX_STEPS2 = new IntHyperDefinition("max-steps2", 3, 200);
    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 2000);
    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS2 = new IntHyperDefinition("max-attempts2", 1, 2000);
    public static final DoubleHyperDefinition HYPER_FRAC_ANCESTOR_SWAP = new DoubleHyperDefinition("fraction-ancestor-swap", 0.0, 1.0);
    public static final DoubleHyperDefinition HYPER_FRAC_ANCESTOR_SWAP2 = new DoubleHyperDefinition("fraction-ancestor-swap2", 0.0, 1.0);

    @Test
    void fix() {

        CollectingHyperManager hyperCollector = new CollectingHyperManager();
        registerStage3(hyperCollector.group("stage3"));
        hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);

        assertThat(hyperCollector.getHyperDefinitions().stream().map(HyperDefinition::getName)).containsExactly(
                "stage3.stage2.stage1.fraction-ancestor-swap",
                "stage3.stage2.stage1.fraction-ancestor-swap2",
                "stage3.stage2.max-attempts",
                "stage3.stage2.max-attempts2",
                "stage3.max-steps",
                "stage3.max-steps2",
                "arrow-mix-strength"
        );

        RemoteHyperManager remoteHyperManager = new RemoteHyperManager(hyperCollector.getHyperDefinitions());

        remoteHyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.3);
        fixStage3(remoteHyperManager.group("stage3"));
        remoteHyperManager.fixLines("""
                stage3.max-steps2 22
                """);

        assertThat(remoteHyperManager.requiresRemoteCall()).isFalse();
    }

    private void registerStage3(HyperManager hyperCollector) {
        registerStage2(hyperCollector.group("stage2"));
        hyperCollector.get(HYPER_MAX_STEPS);
        hyperCollector.get(HYPER_MAX_STEPS2);
    }

    private void registerStage2(HyperManager hyperCollector) {
        registerStage1(hyperCollector.group("stage1"));
        hyperCollector.get(HYPER_MAX_ATTEMPTS);
        hyperCollector.get(HYPER_MAX_ATTEMPTS2);
    }

    private void registerStage1(HyperManager hyperCollector) {
        hyperCollector.get(HYPER_FRAC_ANCESTOR_SWAP);
        hyperCollector.get(HYPER_FRAC_ANCESTOR_SWAP2);
    }

    private void fixStage3(HyperManager hyperManager) {
        fixStage2(hyperManager.group("stage2"));
        hyperManager.fixLines("""
                stage2.max-attempts2 202
                """);
        hyperManager.fix(HYPER_MAX_STEPS, 20);
    }

    private void fixStage2(HyperManager hyperManager) {
        fixStage1(hyperManager.group("stage1"));
        hyperManager.fixLines("""
                stage1.fraction-ancestor-swap2 0.7
                """);
        hyperManager.fix(HYPER_MAX_ATTEMPTS, 40);
    }

    private void fixStage1(HyperManager hyperManager) {
        hyperManager.fix(HYPER_FRAC_ANCESTOR_SWAP, 0.1);
    }


}
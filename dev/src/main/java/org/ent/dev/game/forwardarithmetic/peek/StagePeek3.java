package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.ReadOperandsEntListener;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.util.RandomUtil;
import org.ent.util.Tools;

import java.io.IOException;
import java.time.Duration;

public class StagePeek3 extends StageBase<StagePeek3.Solution> {

    private static final int UPSTREAM_MAX_USAGES = 1000;
    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 400);

    public static final String HYPER_GROUP_STAGE2 = "stage2";

    private final int numberOfNodes;
    private final int maxSteps;

    private final ValueDrawing drawing;

    private final StagePeek2 stagePeek2;
    private final UniformRandomProvider randNetSeeds;

    private StagePeek1.Solution currentUpstream;
    private int currentUpstreamUsages;

    private int numEvaluation;

    public StagePeek3(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingPeek3(hyperManager);

        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodes = hyperManager.get(HYPER_NO_NODES);

        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.stagePeek2 = new StagePeek2(
                hyperManager.group(HYPER_GROUP_STAGE2),
                RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public static void main(String[] args) throws IOException {
        new StagePeek3Factory().main(1);
    }

    public static class StagePeek3Factory extends StageBaseFactory<StagePeek3> {
        @Override
        public StagePeek3 createStage(RemoteHyperManager hyperManager) {
            StagePeek3 dev = new StagePeek3(hyperManager, RandomUtil.newRandom2(randomTrials.nextLong()));
            dev.setTrialMaxEvaluations(2000);
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek2.StagePeek2Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE2));
            hyperCollector.get(HYPER_FRAC_PORTALS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_NO_NODES);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek2.StagePeek2Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE2));
            hyperManager.fix(HYPER_FRAC_PORTALS, 0.43);
            hyperManager.fix(HYPER_NO_NODES, 50);
            hyperManager.fix(HYPER_MAX_STEPS, 100);
        }
    }

    @Override
    protected void printRunInfo(Duration duration) {
        log.info("TOTAL DURATION: {}", duration);
        log.info(" hits: {}", Tools.rate(numHit, numEvaluation));
    }

    @Override
    protected void nextEvaluation() {
        StagePeek1.Solution upstream = getUpstream();

        long netSeed = this.randNetSeeds.nextLong();
        Net net = buildReadNet(netSeed);

        ArithmeticForwardGame game = stagePeek2.setUpGame(upstream, net);
        game.setMaxSteps(maxSteps);
        ReadOperandsEntListener readOperandsListener = new ReadOperandsEntListener(game);
        game.getEnt().addEventListener(readOperandsListener);

        game.execute();

        boolean hit = readOperandsListener.operationData().numTransfer
                      + readOperandsListener.operand1Data().numTransfer
                      + readOperandsListener.operand2Data().numTransfer > 0;
        if (hit) {
            numHit++;
        }
        numEvaluation++;
    }

    private StagePeek1.Solution getUpstream() {
        if (currentUpstream != null && currentUpstreamUsages < UPSTREAM_MAX_USAGES) {
            currentUpstreamUsages++;
        } else {
            currentUpstream = stagePeek2.getNextSolution();
            currentUpstreamUsages = 0;
        }
        return currentUpstream;
    }

    private Net buildReadNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }

    private class ValueDrawingPeek3 extends ValueDrawingHp {

        public ValueDrawingPeek3(HyperManager hyperManager) {
            super(hyperManager);
        }

        @Override
        protected DistributionNode initializeDistribution() {
            double fracPortal = hyperManager.get(HYPER_FRAC_PORTALS);
            log.info("got HPs: fracPortal={}", fracPortal);
            PortalValue portal0And1 = new PortalValue(0, 1);
            return new DistributionSplit(fracPortal)
                    .first(new DistributionLeaf().add(portal0And1))
                    .rest(new DistributionLeaf().add(Operations.SET_VALUE_OPERATION));
        }
    }

    public record Solution() {
    }

}

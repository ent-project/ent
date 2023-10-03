package org.ent.dev.game.enrich;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.Ent;
import org.ent.dev.EntHash;
import org.ent.dev.game.Study;
import org.ent.dev.hyper.DoubleHyperDefinition;
import org.ent.dev.hyper.HyperManager;
import org.ent.dev.hyper.IntHyperDefinition;
import org.ent.dev.hyper.RemoteHyperManager;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHyper;
import org.ent.dev.variation.ArrowMixMutation;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.Node;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.RandomUtil;
import org.ent.run.EntRunner;
import org.ent.util.Logging;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class EnrichInitial extends Study<Chain> {

    private static final boolean WEB_UI = true;
    private static final boolean REPLAY_HITS = false || WEB_UI;

    private static final int NUM_TRIALS = 1;
    private static final int TRIAL_MAX_EVALUATIONS = 200;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 800);
    public static final IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 1000);
    public static final DoubleHyperDefinition HYPER_LINK_RATE = new DoubleHyperDefinition("link-rate", 0.0, 1.0);

    private final int maxSteps;
    private final int numberOfNodes;
    public final ValueDrawing drawing;

    private final HyperManager hyperManager;

    private final UniformRandomProvider randNetSeeds;

    private final EnrichHelper enrichHelper;

    public EnrichInitial(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);
        this.hyperManager = hyperManager;
        this.drawing = new ValueDrawingHyper(hyperManager);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodes = hyperManager.get(HYPER_NO_NODES);
        this.randNetSeeds = RandomUtil.newRandomNoScramble(randMaster.nextLong());
        this.enrichHelper = new EnrichHelper(this);
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new Factory().start(NUM_TRIALS);
    }

    public static class Factory extends StudyFactory<EnrichInitial> {

        protected final UniformRandomProvider randomTrials = RandomUtil.newRandom(12349L);

        @Override
        public EnrichInitial createStage(RemoteHyperManager hyperManager, int indexTrial) {
            EnrichInitial dev = new EnrichInitial(hyperManager, RandomUtil.newRandomNoScramble(randomTrials.nextLong()));
            dev.setTrialMaxEvaluations(TRIAL_MAX_EVALUATIONS);
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            ValueDrawingHyper.registerHyperparameter(hyperCollector);
            hyperCollector.get(HYPER_NO_NODES);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_LINK_RATE);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            hyperManager.fix(ValueDrawingHyper.FRAC_COMMANDS, 0.8);
            hyperManager.fix(ValueDrawingHyper.FRAC_MAJOR_COMMANDS, 0.7);
            hyperManager.fix(ValueDrawingHyper.FRAC_MAJOR_SPLIT, 0.6);
            hyperManager.fix(ValueDrawingHyper.FRAC_SET, 0.4);
            hyperManager.fix(HYPER_NO_NODES, 20);
            hyperManager.fix(HYPER_MAX_STEPS, 12);
            hyperManager.fix(HYPER_LINK_RATE, 0.4);
        }
    }

    @Override
    protected void nextEvaluation() {

        EnrichGame game = new EnrichGame(maxSteps, EnrichHelper.HAS_COMMON);
        game.installModificationTracker();

        Chain chain = new Chain(game, indexTrial, indexEvaluation);
        for (int i = 0; i < 5; i++) {
            long netSeed = randNetSeeds.nextLong();
            Net net = buildNet(game, netSeed);
            chain.pieces.add(new Piece(net, netSeed));
        }

        for (Piece piece : chain.pieces) {
            game.resetStage();
            game.ent().setNet(piece.net);
            game.execute();
            piece.contributing = game.getModificationTracker().wasModified();
        }

        chain.staticFitness = StaticValuation.calculateFitness(game.netAnt());

        log.info("{} static fitness: {}", indexEvaluation, chain.staticFitness);
        if (chain.staticFitness > 2) {
            chain.executionFitness = determineExecutionFitness(game.netAnt(), false);
            log.info("{} execution fitness: {}", indexEvaluation, chain.executionFitness);
            if (REPLAY_HITS) {
                WebUiStoryOutput.addStoryWithAnnouncement(chain.getReplayWithDetailsStoryName(),
                        () -> enrichHelper.replayWithDetails(chain));
                WebUiStoryOutput.addStoryWithAnnouncement("Enrich-%s-%s ant execution".formatted(indexTrial, indexEvaluation),
                        () -> determineExecutionFitness(game.netAnt(), true));
            }
            if (chain.executionFitness > 0.1) {
                submitSolution(chain);
            }
        }
    }

    public double determineExecutionFitness(Net antOrig, boolean verbose) {
        if (verbose) log.info("determining Execution Fitness {}-{}", indexTrial, indexEvaluation);
        Net net = NetCopy2.createCopy(antOrig);
        Ent ent = new Ent(net);
        AntNetFitnessTracker trackerNet = new AntNetFitnessTracker(true);
        net.addEventListener(trackerNet);
        AntEntFitnessTracker trackerEnt = new AntEntFitnessTracker();
        ent.addEventListener(trackerEnt);
        EntRunner runner = new EntRunner(ent);

        NetFormatter formatter = null;
        if (verbose) {
            formatter = new NetFormatter();
            log.info("ant \n{}", formatter.format(ent));
            Logging.logDot(ent);
        }

        Set<Integer> hashes = new HashSet<>();
        int netHash = EntHash.hash(net);
        hashes.add(netHash);
        int maxStepsAntPlay = 15;
        int step = 1;
        while (step < maxStepsAntPlay) {
            runner.step();

            if (verbose) {
                log.info(Logging.JUMP_MARKER, "after step {}:\n{}", step, formatter.format(ent));
                Logging.logDot(ent);
            }

            int newNetHash = EntHash.hash(net);
            if (hashes.contains(newNetHash)) {
                if (verbose) log.info("repetition detected!");
                break;
            }
            hashes.add(newNetHash);
            step++;
        }
        double fitness = trackerNet.getFitness();
        fitness += 2 * StaticValuation.limitFunction(step, 30);
        fitness += 20 * StaticValuation.limitFunction(trackerEnt.commandsExecuted.size(), 10);
        if (verbose) log.info("done determining execution fitness: {}", fitness);
        return fitness;
    }

    public Net buildNet(EnrichGame game, long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(
                numberOfNodes, RandomUtil.newRandomNoScramble(netSeed), drawing);
        Net net = netCreator.drawNet();

        DomainArrowInjection injection = new DomainArrowInjection(
                hyperManager,
                net,
                RandomUtil.newRandomNoScramble(RandomUtil.lightScramble(netSeed)),
                game);
        injection.execute();

        return net;
    }

    private static class DomainArrowInjection extends ArrowMixMutation {

        private final Node[] targets;

        public DomainArrowInjection(HyperManager hyperManager, Net net, UniformRandomProvider rand, EnrichGame game) {
            super(hyperManager.get(HYPER_LINK_RATE), net, rand);
            if (EnrichHelper.HAS_COMMON) {
                targets = new Node[]{
                        game.rootCommon,
                        game.rootCommon,
                        game.rootCommon,
                        game.rootCommon,
                        game.rootCommon2,
                        game.rootCommon2,
                        game.rootCommon3,
                        game.rootCommon4,
                        game.netAnt().getRoot(),
                        game.netAnt().getRoot(),
                        game.netAnt().getRoot()
                };
            } else {
                targets = new Node[]{
                        game.netAnt().getRoot()
                };
            }
            setDestinationRange(0, targets.length);
        }

        @Override
        protected Node resolveTargetNode(int indexTargetResolved) {
            return targets[indexTargetResolved];
        }

        @Override
        protected ArrowDirection drawArrowDirection() {
            return ArrowDirection.LEFT;
        }
    }
}
package org.ent.dev.game.enrich;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.Study;
import org.ent.dev.hyper.HyperManager;
import org.ent.dev.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.util.RandomUtil;
import org.ent.util.Logging;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnrichEvo extends Study<Chain> {

    private static final boolean WEB_UI = true;
    private static final boolean REPLAY_HITS = false || WEB_UI;

    public static final int NUM_TRIALS = 1;
    public static final int MAX_EVALUATIONS = 20;

    public static final int POOL_SIZE = 30;

    public static final String HYPER_GROUP_INIT = "init";

    private final EnrichInitial enrichInitial;
    private final List<Chain> pool = new ArrayList<>();

    private final EnrichHelper enrichHelper;
    private final UniformRandomProvider randCrossover;

    public EnrichEvo(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);
        this.randCrossover = RandomUtil.newRandomNoScramble(randMaster.nextLong());
        this.enrichInitial = new EnrichInitial(
                hyperManager.group(HYPER_GROUP_INIT),
                RandomUtil.newRandom(randMaster.nextLong()));
        this.enrichHelper = new EnrichHelper(enrichInitial);
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new Factory().start(NUM_TRIALS);
    }

    public static class Factory extends StudyFactory<EnrichEvo> {

        @Override
        public EnrichEvo createStage(RemoteHyperManager hyperManager, int indexTrial) {
            EnrichEvo enrichEvo = new EnrichEvo(hyperManager, RandomUtil.newRandom(randomTrials.nextLong()));
            enrichEvo.setTrialMaxEvaluations(MAX_EVALUATIONS);
            return enrichEvo;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new EnrichInitial.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_INIT));

        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new EnrichInitial.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_INIT));
        }
    }

    @Override
    public void runTrial(int indexTrial) {
        enrichInitial.initializeTrial(indexTrial);
        pool.clear();
        for (int i = 0; i < POOL_SIZE; i++) {
            pool.add(enrichInitial.getNextSolution());
        }
        super.runTrial(indexTrial);
    }

    @Override
    protected void nextEvaluation() {
        int index1 = randCrossover.nextInt(pool.size());
        Chain chain1 = pool.get(index1);
        int index2 = randCrossover.nextInt(pool.size());
        Chain chain2 = pool.get(index2);
        EnrichGame game = new EnrichGame(enrichInitial.getMaxSteps(), EnrichHelper.HAS_COMMON);
        game.installModificationTracker();
        Chain chain = crossover(chain1, chain2, game, randCrossover);

        for (Piece piece : chain.pieces) {
            game.resetStage();
            game.ent().setNet(piece.net);
            game.execute();
            piece.contributing = game.getModificationTracker().wasModified();
        }

        chain.staticFitness = StaticValuation.calculateFitness(game.netAnt());

        log.info("{} static fitness: {}", indexEvaluation, chain.staticFitness);
        chain.executionFitness = enrichInitial.determineExecutionFitness(game.netAnt(), false);
        log.info("{} execution fitness: {}", indexEvaluation, chain.executionFitness);

        Logging.htmlLogger.info("{} + {} -> {}",
                WebUiStoryOutput.getStoryLink(chain1.getReplayWithDetailsStoryName(), Double.toString(chain1.executionFitness)),
                WebUiStoryOutput.getStoryLink(chain2.getReplayWithDetailsStoryName(), Double.toString(chain2.executionFitness)),
                chain.executionFitness
        );
        if (REPLAY_HITS) {
            WebUiStoryOutput.addStoryWithAnnouncement("Enrich-crossover-%s-%s".formatted(index1, index2),
                    () -> enrichHelper.replayWithDetails(chain));
            WebUiStoryOutput.addStoryWithAnnouncement("Enrich-crossover-%s-%s ant execution".formatted(index1, index2),
                    () -> enrichInitial.determineExecutionFitness(game.netAnt(), true));
        }
    }

    private Chain crossover(Chain chain1, Chain chain2, EnrichGame game, UniformRandomProvider randCrossover) {
        Chain chainNew = new Chain(game);
        int cut1 = randCrossover.nextInt(chain1.pieces.size() + 1); // cut first chain before index cut1
        int cut2 = randCrossover.nextInt(chain2.pieces.size() + 1); // cut second chain before index cut2
        for (int i = 0; i < cut1; i++) {
            chainNew.pieces.add(copyPiece(game, chain1.pieces.get(i)));
        }
        for (int i = cut2; i < chain2.pieces.size(); i++) {
            chainNew.pieces.add(copyPiece(game, chain2.pieces.get(i)));
        }
        return chainNew;
    }

    private Piece copyPiece(EnrichGame game, Piece piece0) {
        Net net = enrichInitial.buildNet(game, piece0.seed);
        return new Piece(net, piece0.seed);
    }
}

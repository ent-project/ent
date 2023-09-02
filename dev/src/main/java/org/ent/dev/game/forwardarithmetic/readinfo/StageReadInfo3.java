package org.ent.dev.game.forwardarithmetic.readinfo;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.PortalMoveEntEventListener;
import org.ent.dev.game.forwardarithmetic.ReadOperandsEntListener;
import org.ent.dev.game.Study;
import org.ent.dev.game.forwardarithmetic.Util;
import org.ent.dev.trim2.TrimmingHelper;
import org.ent.dev.trim2.TrimmingListener;
import org.ent.hyper.CollectingHyperManager;
import org.ent.hyper.HyperManager;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.NetCopyPack;
import org.ent.net.util.RandomUtil;
import org.ent.util.Logging;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Clean up the 2 Ents from previous stage
 */
public class StageReadInfo3 extends Study<StageReadInfo3.Solution> {

    private static final boolean WEB_UI = true;

    private static final Logger logStatic = LoggerFactory.getLogger(StageReadInfo3.class);

    StageReadInfo2 stageReadInfo2;

    public StageReadInfo3(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);
        this.stageReadInfo2 = new StageReadInfo2(hyperManager, RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public static void main(String[] args) {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        UniformRandomProvider randomMain = RandomUtil.newRandom2(12345L);
        CollectingHyperManager collector = new CollectingHyperManager();
        StageReadInfo2.registerHyperparameter(collector);

        RemoteHyperManager hyperManager = new RemoteHyperManager(collector.getHyperDefinitions());
        hyperManager.group(StageReadInfo2.HYPER_GROUP_STAGE1).fixJson(StageReadInfo1.HYPER_SELECTION);
        hyperManager.group(StageReadInfo2.HYPER_GROUP_THIS).fixLines(StageReadInfo2.HYPER_SELECTION);

        StageReadInfo3 stageReadInfo3 = new StageReadInfo3(hyperManager, RandomUtil.newRandom2(randomMain.nextLong()));

        for (int i = 0; i < 10; i++) {
            Solution solution = stageReadInfo3.getNextSolution();
            String runId = "run-" + i;
            WebUiStoryOutput.addStory(runId, () -> replay(solution));
            Logging.logHtml(() -> "<a href=\"/?story=%s\" target=\"_blank\">%s</a>".formatted(runId, runId));
        }
    }

    private static void replay(Solution solution) {
        ArithmeticForwardGame game0 = solution.upstream.upstream().game();
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                solution.net1,
                solution.steps1);
        game.setVerbose(true);
        ReadOperandsEntListener readOperandsListener = new ReadOperandsEntListener(game);
        game.getEnt().addEventListener(readOperandsListener);

        game.execute();

        logStatic.info("Skipped {} steps.", solution.skippedInPhase2);

        game.getEnt().setNet(solution.net2);
        game.setMaxSteps(solution.steps2);

        game.execute();
    }

    public static class Solution {
        public StageReadInfo2.Solution upstream;
        public Net net1;
        public int steps1;
        public Net net2;
        public int steps2;
        public int skippedInPhase2;
    }

    @Override
    protected void printRunInfo(Duration duration) {
        // nothing to print
    }

    @Override
    protected void nextEvaluation() {
        StageReadInfo2.Solution upstream = stageReadInfo2.getNextSolution();
        Solution solution = new Solution();
        solution.upstream = upstream;
        cleanUpPart1(upstream.upstream(), solution);
        cleanUpPart2(upstream, solution);
        submitSolution(solution);
    }

    private void cleanUpPart1(StageReadInfo1.Solution upstream1, Solution solution) {
        PortalMoveEntEventListener portalMoveListener = upstream1.portalMoveEntEventListener();
        ArithmeticForwardGame game0 = upstream1.game();

        Net net = this.stageReadInfo2.getStageReadInfo1().buildNet(upstream1.netSeed());
        int stepsForward = portalMoveListener.firstTimePortalMoved();
        ArithmeticForwardGame gameAdvance = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                stepsForward
        );

        gameAdvance.execute();

        Net advancedBlueprint = NetCopy2.createCopy(net);

        Net advancedNet = NetCopy2.createCopy(advancedBlueprint);
        int realSteps = portalMoveListener.lastTimePortalMoved() - portalMoveListener.firstTimePortalMoved() + 1;
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                advancedNet,
                realSteps);
        TrimmingListener trimmingListener = new TrimmingListener(advancedNet.getNodes().size());
        advancedNet.addEventListener(trimmingListener);

        game.execute();

        Net advancedNetForTrimming = NetCopy2.createCopy(advancedBlueprint);
        TrimmingHelper.trim(advancedNetForTrimming, trimmingListener);
        // FIXME: maybe packing copy can be delayed
        Net copy = new NetCopyPack(advancedNetForTrimming).createCopy();
        solution.net1 = copy;
        solution.steps1 = realSteps;
    }

    private void cleanUpPart2(StageReadInfo2.Solution upstream2, Solution solution) {
        PortalMoveEntEventListener portalMoveListener = investigatePortalMoves(upstream2);

        Net net = stageReadInfo2.buildReadNet(upstream2.netSeed());
        ArithmeticForwardGame gameAdvance = stageReadInfo2.setUpGame(upstream2.upstream(), net);
        ReadOperandsEntListener readOperandsListener = upstream2.readOperandsListener();
        int firstInterestingStep = Util.min(readOperandsListener.firstTransfer(), portalMoveListener.firstTimePortalMoved());
        gameAdvance.setMaxSteps(firstInterestingStep);

        gameAdvance.execute();

        Net advancedBlueprint = NetCopy2.createCopy(net);

        Net advancedNet = NetCopy2.createCopy(advancedBlueprint);
        ArithmeticForwardGame game = stageReadInfo2.setUpGame(upstream2.upstream(), advancedNet);
        int lastInterestingStep = readOperandsListener.lastTransfer();
        int realSteps = lastInterestingStep - firstInterestingStep + 1;
        game.setMaxSteps(realSteps);
        TrimmingListener trimmingListener = new TrimmingListener(advancedNet.getNodes().size());
        advancedNet.addEventListener(trimmingListener);

        game.execute();

        Net advancedNetForTrimming = NetCopy2.createCopy(advancedBlueprint);
        TrimmingHelper.trim(advancedNetForTrimming, trimmingListener);
        // FIXME: maybe packing copy can be delayed
        Net copy = new NetCopyPack(advancedNetForTrimming).createCopy();
        solution.net2 = copy;
        solution.steps2 = realSteps;
        solution.skippedInPhase2 = firstInterestingStep;
    }

    @NotNull
    private PortalMoveEntEventListener investigatePortalMoves(StageReadInfo2.Solution upstream2) {
        Net net = stageReadInfo2.buildReadNet(upstream2.netSeed());
        ArithmeticForwardGame game = stageReadInfo2.setUpGame(upstream2.upstream(), net);
        PortalMoveEntEventListener portalMoveEntEventListener = new PortalMoveEntEventListener(game);
        game.getEnt().addEventListener(portalMoveEntEventListener);
//        game.setMaxSteps(upstream2.readOperandsListener().lastTransfer() + 1);

        game.execute();
        return portalMoveEntEventListener;
    }
}

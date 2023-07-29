package org.ent.dev.game.forwardarithmetic.readinfo;

import org.ent.LazyPortalArrow;
import org.ent.NopEntEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.run.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Record portal moves.
 *
 * This is only tracked up to the first eval-flow on the verifier,
 * i.e. we do not want the verifier changed at this point.
 */
public class PortalMoveEntEventListener extends NopEntEventListener {

    private static final Logger log = LoggerFactory.getLogger(PortalMoveEntEventListener.class);

    private final TargetTracker targetTracker1, targetTracker2;
    private final ArithmeticForwardGame game;
    private Integer firstTimePortalMoved;
    private Integer lastTimePortalMoved;
    private Integer lastAnswerValue;
    private Integer firstVerifierChange;

    PortalMoveEntEventListener(ArithmeticForwardGame game) {
        this.game = game;
        this.targetTracker1 = new TargetTracker(game.getVerifierPortal1());
        this.targetTracker2 = new TargetTracker(game.getVerifierPortal2());
    }

    public TargetTracker targetTracker1() {
        return targetTracker1;
    }

    public TargetTracker targetTracker2() {
        return targetTracker2;
    }

    public Integer firstTimePortalMoved() {
        return firstTimePortalMoved;
    }

    public Integer lastTimePortalMoved() {
        return lastTimePortalMoved;
    }

    public Integer lastAnswerValue() {
        return lastAnswerValue;
    }

    @Override
    public void afterCommandExecution(StepResult stepResult) {
        if (game.getVerifierNet() != null) {
            if (firstVerifierChange == null) {
                if (game.isVerifierChanged()) {
                    firstVerifierChange = game.getStep();
                }
            }
            if (firstVerifierChange == null) {
                targetTracker1.recordPortal();
                targetTracker2.recordPortal();
            }
        }
    }

    public int totalTargetChanges() {
        return targetTracker1.targetChanges + targetTracker2.targetChanges;
    }

    private void recordAnswerValue() {
        lastAnswerValue = game.getAnswerNode().getValue(Purview.DIRECT);
    }

    public class TargetTracker {
        private final LazyPortalArrow verifierPortal;
        private Node lastTarget;
        private int targetChanges;

        TargetTracker(LazyPortalArrow verifierPortal) {
            this.verifierPortal = verifierPortal;
            initialize();
        }

        private void initialize() {
            if (verifierPortal.isInitialized()) {
                lastTarget = verifierPortal.getTarget(Purview.DIRECT);
            }
        }

        public Node lastTarget() {
            return lastTarget;
        }

        public void recordPortal() {
            if (verifierPortal.isInitialized()) {
                Node target = verifierPortal.getTarget(Purview.DIRECT);

                if (target == game.getVerifierNetOriginalRoot()) {
                    target = null;
                }
                if (target != lastTarget) {
                    if (game.isVerbose()) {
                        log.info("event: portal moved");
                    }
                    if (totalTargetChanges() == 0) {
                        firstTimePortalMoved = game.getStep();
                    }
                    lastTimePortalMoved = game.getStep();
                    recordAnswerValue();
                    targetChanges++;
                    lastTarget = target;
                }
            }
        }
    }
}

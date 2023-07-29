package org.ent.dev.game.forwardarithmetic.readinfo;

import org.ent.LazyPortalArrow;
import org.ent.NopEntEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.run.StepResult;

/**
 * Record portal moves.
 *
 * This is only tracked up to the first eval-flow on the verifier,
 * i.e. we do not want the verifier changed at this point.
 */
public class PortalMoveEntEventListener extends NopEntEventListener {
    private final TargetTracker targetTracker1, targetTracker2;
    private final ArithmeticForwardGame game;
    private Integer firstTimePortalMoved;
    private Integer lastTimePortalMoved;
    private Integer lastAnswerValue;
    private Integer firstEvalFlowOnVerifier;

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
            if (firstEvalFlowOnVerifier == null) {
                if (game.getVerifierNet().getRoot() != game.getVerifierNetOriginalRoot()) {
                    firstEvalFlowOnVerifier = game.getStep();
                }
            }
            if (firstEvalFlowOnVerifier == null) {
                recordPortal(targetTracker1);
                recordPortal(targetTracker2);
            }
        }
    }

    public int totalTargetChanges() {
        return targetTracker1.targetChanges + targetTracker2.targetChanges;
    }

    private void recordPortal(TargetTracker tracker) {
        if (tracker.verifierPortal.isInitialized()) {
            Node target = tracker.verifierPortal.getTarget(Purview.DIRECT);

            if (target == game.getVerifierNetOriginalRoot()) {
                target = null;
            }
            boolean targetChanged = target != tracker.lastTarget;
            if (targetChanged) {
                if (totalTargetChanges() == 0) {
                    firstTimePortalMoved = game.getStep();
                }
                lastTimePortalMoved = game.getStep();
                recordAnswerValue();
                tracker.targetChanges++;
                tracker.lastTarget = target;
            }
        }
    }

    private void recordAnswerValue() {
        lastAnswerValue = game.getAnswerNode().getValue(Purview.DIRECT);
    }

    public static class TargetTracker {
        private final LazyPortalArrow verifierPortal;
        private Node lastTarget;
        private int targetChanges;

        TargetTracker(LazyPortalArrow verifierPortal) {
            this.verifierPortal = verifierPortal;
        }

        public Node lastTarget() {
            return lastTarget;
        }
    }
}

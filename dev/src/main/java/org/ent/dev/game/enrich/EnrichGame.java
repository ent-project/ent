package org.ent.dev.game.enrich;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.ent.util.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ent.util.Logging.JUMP_MARKER;

public class EnrichGame {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private NetFormatter formatter;

    private Net netCommon;
    Node rootCommon;
    Node rootCommon2;
    Node rootCommon3;
    Node rootCommon4;
    private Net netAnt;
    private Ent ent;

    private int step;

    private boolean verbose;
    private final boolean hasCommon;

    private final int maxSteps;

    private Runnable beforeStep;
    private Runnable afterStep;

    private boolean executionStopped;

    private ModificationTracker modificationTracker;

    public EnrichGame(int maxSteps, boolean hasCommon) {
        this.maxSteps = maxSteps;
        this.hasCommon = hasCommon;
        buildEnt();
    }

    public Ent ent() {
        return ent;
    }

    public Net netAnt() {
        return netAnt;
    }

    public void resetStage() {
        this.step = 0;
        if (this.modificationTracker != null) {
            this.modificationTracker.reset();
        }
    }

    public void installModificationTracker() {
        if (this.modificationTracker != null) {
            throw new IllegalStateException();
        }
        this.modificationTracker = new ModificationTracker();
        netAnt.addEventListener(modificationTracker);
    }

    public ModificationTracker getModificationTracker() {
        return modificationTracker;
    }

    public void setBeforeStep(Runnable beforeStep) {
        this.beforeStep = beforeStep;
    }

    public void setAfterStep(Runnable afterStep) {
        this.afterStep = afterStep;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        this.formatter = new NetFormatter();
    }

    public void stopExecution() {
        this.executionStopped = true;
    }

    public void buildEnt() {
        ent = new Ent(new Net());
        if (hasCommon) {
            netCommon = new Net();
            netCommon.setName("common");
            rootCommon = netCommon.newNode(Permissions.DIRECT);
            netCommon.setRoot(rootCommon);
            ent.addDomain(netCommon);

            rootCommon2 = netCommon.newNode(Permissions.DIRECT);
            rootCommon3 = netCommon.newNode(Permissions.DIRECT);
            rootCommon4 = netCommon.newNode(Permissions.DIRECT);
            netCommon.setAnnotation(rootCommon2, "r2");
            netCommon.setAnnotation(rootCommon3, "r3");
            netCommon.setAnnotation(rootCommon4, "r4");
            netCommon.addSecondaryRoot(rootCommon2);
            netCommon.addSecondaryRoot(rootCommon3);
            netCommon.addSecondaryRoot(rootCommon4);
        }

        netAnt = new Net();
        netAnt.setName("ant");
        Node rootAnt = netAnt.newNode(Permissions.DIRECT);
        netAnt.setRoot(rootAnt);
        ent.addDomain(netAnt);

        if (hasCommon) {
            ent.putPermissions(p -> p
                    .net(np -> np
                            .canModify(netCommon)
                            .canModify(netAnt))
                    .domain(netCommon, np -> np
                            .canModify(netAnt)));
        } else {
            ent.putPermissions(p -> p
                    .net(np -> np
                            .canModify(netAnt)));
        }
    }

    public void execute() {
        EntRunner runner = new EntRunner(ent);
        this.executionStopped = false;
        if (verbose) {
            log.info("ent \n{}", formatter.format(ent));
            Logging.logDot(ent);
        }
        while (step < maxSteps) {
            if (beforeStep != null) {
                beforeStep.run();
            }
            if (this.executionStopped) {
                if (verbose) {
                    log.info("execution stopped before step {}", step);
                }
                break;
            }

            StepResult result = runner.step();

            if (afterStep != null) {
                afterStep.run();
            }
            if (verbose) {
                log.info(JUMP_MARKER, "after step {}:\n{}", step, formatter.format(ent));
                Logging.logDot(ent);
            }
            if (this.executionStopped) {
                if (verbose) {
                    log.info("execution stopped after step {}", step);
                }
                break;
            }
            if (result.equals(StepResult.CONCLUDED)) {
                if (verbose) {
                    log.info("execution concluded after step {}", step);
                }
                break;
            }
            step++;
        }
    }
}
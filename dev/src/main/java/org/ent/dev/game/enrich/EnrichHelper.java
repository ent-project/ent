package org.ent.dev.game.enrich;

import org.ent.dev.EntHash;
import org.ent.dev.trim.TrimmingHelper;
import org.ent.dev.trim.TrimmingListener;
import org.ent.net.Net;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.ent.util.Logging.MAJOR_JUMP_MARKER;

public class EnrichHelper {

    private final static Logger log = LoggerFactory.getLogger(EnrichHelper.class);

    public static final boolean HAS_COMMON = false;

    private final EnrichInitial enrichInitial;

    public EnrichHelper(EnrichInitial enrichInitial) {
        this.enrichInitial = enrichInitial;
    }

    public void replayWithDetails(Chain chain) {
        doReplayWithDetails(trim(chain));
    }

    private Chain trim(Chain chainInput) {
        EnrichGame gamePre = new EnrichGame(enrichInitial.getMaxSteps(), HAS_COMMON);
        EnrichGame game = new EnrichGame(enrichInitial.getMaxSteps(), HAS_COMMON);
        Chain chainResult = new Chain(game);
        for (Piece piece : chainInput.pieces) {
            if (!piece.contributing) {
                chainResult.pieces.add(null);
            } else {
                TrimmingListener trimmingListener = runForTrimming(gamePre, piece);

                Net net = enrichInitial.buildNet(game, piece.seed);
                TrimmingHelper.trim(net, trimmingListener);
                chainResult.pieces.add(new Piece(net, piece.seed));
            }
        }
        return chainResult;
    }

    private TrimmingListener runForTrimming(EnrichGame game, Piece piece) {
        Net net = enrichInitial.buildNet(game, piece.seed);

        TrimmingListener trimmingListener = new TrimmingListener(net.getNodes().size());
        net.addEventListener(trimmingListener);

        game.resetStage();
        game.ent().setNet(net);
        game.execute();

        return trimmingListener;
    }

    private void doReplayWithDetails(Chain chain) {
        EnrichGame game = chain.game;
        game.setVerbose(true);
        for (int i = 0; i < chain.pieces.size(); i++) {
            Piece piece = chain.pieces.get(i);
            log.info(MAJOR_JUMP_MARKER, "-------- Starting piece #{} ---------", i);
            if (piece == null) {
                log.info("not contributing - skipping piece {}", i);
            } else {
                game.resetStage();
                game.ent().setNet(piece.net);
                Set<Integer> hashes = new HashSet<>();
                game.setBeforeStep(() -> {
                    int hash = EntHash.hash(game.ent());
                    boolean isNew = hashes.add(hash);
                    if (!isNew) {
                        log.info("repetition detected, stopping execution");
                        game.stopExecution();
                    }
                });
                game.execute();
            }
        }
    }

}

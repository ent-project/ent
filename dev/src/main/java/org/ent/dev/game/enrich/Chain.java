package org.ent.dev.game.enrich;

import java.util.ArrayList;
import java.util.List;

public class Chain {
    public EnrichGame game;
    public List<Piece> pieces = new ArrayList<>();
    public double staticFitness;
    public double executionFitness;
    public Integer indexTrial;
    public Integer indexEvaluation;

    public Chain(EnrichGame game) {
        this.game = game;
    }

    public Chain(EnrichGame game, int indexTrial, int indexEvaluation) {
        this.game = game;
        this.indexTrial = indexTrial;
        this.indexEvaluation = indexEvaluation;
    }

    public String getReplayWithDetailsStoryName() {
        return "Enrich-%s-%s".formatted(indexTrial, indexEvaluation);
    }
}

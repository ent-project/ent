package org.ent.dev.game.forwardarithmetic.readinfo;

import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;

public class ValueDrawingWithPortals extends ValueDrawingHp {
    public static DoubleHyperDefinition FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);

    public static void registerHyperparameter(HyperManager hyperManager) {
        hyperManager.get(FRAC_PORTALS);
        ValueDrawingHp.registerHyperparameter(hyperManager);
    }

    public ValueDrawingWithPortals(HyperManager hyperManager) {
        super(hyperManager);
    }

    @Override
    protected DistributionNode initializeDistribution() {
        double fracPortal = hyperManager.get(FRAC_PORTALS);
        DistributionNode distribution = defaultDistribution();
        return new DistributionSplit(fracPortal)
                .first(new DistributionLeaf().add(new PortalValue(0, 1), 1.0))
                .rest(distribution);
    }
}

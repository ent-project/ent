package org.ent.dev.hyper;

public class DoubleHyperDefinition extends NumericHyperDefinition<Double> {

    public DoubleHyperDefinition(String name, Double minValue, Double maxValue) {
        super(name, "float", minValue, maxValue);
    }

    @Override
    public HyperDefinition<Double> cloneWithName(String otherName) {
        return new DoubleHyperDefinition(otherName, minValue, maxValue);
    }
}

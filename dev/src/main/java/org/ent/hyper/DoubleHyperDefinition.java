package org.ent.hyper;

public class DoubleHyperDefinition extends NumericHyperDefinition<Double> {

    public DoubleHyperDefinition(String name, Double minValue, Double maxValue) {
        super(name, minValue, maxValue);
        this.type = "float";
    }
}

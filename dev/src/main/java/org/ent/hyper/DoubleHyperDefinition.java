package org.ent.hyper;

public class DoubleHyperDefinition extends AbstractNumericHyperDefinition<Double> {

    public DoubleHyperDefinition(String name, Double minValue, Double maxValue) {
        super(name, "float", minValue, maxValue);
    }

    @Override
    public Double getAverageValue() {
        return (getMinValue() + getMaxValue()) / 2;
    }
}

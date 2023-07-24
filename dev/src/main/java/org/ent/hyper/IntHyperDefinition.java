package org.ent.hyper;

public class IntHyperDefinition extends AbstractNumericHyperDefinition<Integer> {

    public IntHyperDefinition(String name, Integer minValue, Integer maxValue) {
        super(name, "int", minValue, maxValue);
    }

    @Override
    public Integer getAverageValue() {
        return (getMinValue() + getMaxValue()) / 2;
    }
}

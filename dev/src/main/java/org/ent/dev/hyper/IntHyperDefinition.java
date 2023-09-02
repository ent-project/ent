package org.ent.dev.hyper;

public class IntHyperDefinition extends NumericHyperDefinition<Integer> {

    public IntHyperDefinition(String name, Integer minValue, Integer maxValue) {
        super(name, "int", minValue, maxValue);
    }

    @Override
    public HyperDefinition<Integer> cloneWithName(String otherName) {
        return new IntHyperDefinition(otherName, minValue, maxValue);
    }
}

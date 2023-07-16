package org.ent.hyper;

public class IntHyperDefinition extends NumericHyperDefinition<Integer> {

    public IntHyperDefinition(String name, Integer minValue, Integer maxValue) {
        super(name, minValue, maxValue);
        this.type = "int";
    }
}

package org.ent.hyper;

public class FloatHyperDefinition extends NumericHyperDefinition<Float> {

    public FloatHyperDefinition(String name, Float minValue, Float maxValue) {
        super(name, minValue, maxValue);
        this.type = "float";
    }
}

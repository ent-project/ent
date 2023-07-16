package org.ent.hyper;

public abstract class NumericHyperDefinition<T> extends HyperDefinition {

    protected T minValue;

    protected T maxValue;

    public NumericHyperDefinition(String name, T minValue, T maxValue) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) {
        this.minValue = minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(T maxValue) {
        this.maxValue = maxValue;
    }
}

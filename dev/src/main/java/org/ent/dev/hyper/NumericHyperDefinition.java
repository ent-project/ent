package org.ent.dev.hyper;

public abstract class NumericHyperDefinition<T> extends HyperDefinition<T> {

    protected final T minValue;

    protected final T maxValue;

    public NumericHyperDefinition(String name, String type, T minValue, T maxValue) {
        super(name, type);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public T getMinValue() {
        return minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return "NumericHyperDefinition{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}

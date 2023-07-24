package org.ent.hyper;

public abstract class AbstractNumericHyperDefinition<T> extends AbstractHyperDefinition implements NumericHyperDefinition<T> {

    protected final T minValue;

    protected final T maxValue;

    public AbstractNumericHyperDefinition(String name, String type, T minValue, T maxValue) {
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

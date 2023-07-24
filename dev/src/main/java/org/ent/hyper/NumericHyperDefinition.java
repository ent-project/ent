package org.ent.hyper;

public interface NumericHyperDefinition<T> extends HyperDefinition {
    T getMinValue();

    T getMaxValue();

    T getAverageValue();

    default NumericHyperDefinition<T> group(String group) {
        return new SubNumericHyperDefinition<>(this, group);
    }
}

package org.ent.hyper;

public class SubNumericHyperDefinition<T> implements NumericHyperDefinition<T> {

    private final NumericHyperDefinition<T> delegate;
    private final String group;

    public SubNumericHyperDefinition(NumericHyperDefinition<T> delegate, String group) {
        this.delegate = delegate;
        this.group = group;
    }

    @Override
    public String getName() {
        return group + "." + delegate.getName();
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public T getMinValue() {
        return delegate.getMinValue();
    }

    @Override
    public T getMaxValue() {
        return delegate.getMaxValue();
    }

    @Override
    public T getAverageValue() {
        return delegate.getAverageValue();
    }
}

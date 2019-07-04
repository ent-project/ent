package org.ent.dev.hyper;

public abstract class RangedHyperparameter<T> extends AbstractHyperparameter<T> {

	protected T minimumValue;

	protected T maximumValue;

	public RangedHyperparameter(T defaultValue, String description) {
		super(defaultValue, description);
	}

	public T getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(T minimumValue) {
		this.minimumValue = minimumValue;
		validate(value);
		validate(defaultValue);
	}

	public T getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(T maximumValue) {
		this.maximumValue = maximumValue;
		validate(value);
		validate(defaultValue);
	}

}

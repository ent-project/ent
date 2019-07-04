package org.ent.dev.hyper;

public class IntegerHyperparameter extends RangedHyperparameter<Integer> {

	public IntegerHyperparameter(int defaultValue, String description) {
		super(defaultValue, description);
	}

	@Override
	protected void validate(Integer x) {
		if (minimumValue != null && x < minimumValue) {
			throw new IllegalArgumentException("value " + x + " lower than minimum value " + minimumValue);
		}
		if (maximumValue != null && x > maximumValue) {
			throw new IllegalArgumentException("value " + x + " exceeds maximum value " + maximumValue);
		}
	}

}

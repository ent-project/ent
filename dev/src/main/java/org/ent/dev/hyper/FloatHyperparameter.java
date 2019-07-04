package org.ent.dev.hyper;

public class FloatHyperparameter extends RangedHyperparameter<Float> {

	public FloatHyperparameter(float defaultValue, String description) {
		super(defaultValue, description);
	}

	@Override
	protected void validate(Float x) {
		if (minimumValue != null && x < minimumValue) {
			throw new IllegalArgumentException("value " + x + " lower than minimum value " + minimumValue);
		}
		if (maximumValue != null && x > maximumValue) {
			throw new IllegalArgumentException("value " + x + " exceeds maximum value " + maximumValue);
		}
	}

}

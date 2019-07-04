package org.ent.dev.hyper;

import java.beans.PropertyChangeListener;

public interface Hyperparameter<T> {

	T getValue();

	void setValue(T value);

	T getDefaultValue();

	void setDefaultValue(T defaultValue);

	String getDescription();

	void setDescription(String description);

	void addPropertyChangeListener(PropertyChangeListener listener);
}

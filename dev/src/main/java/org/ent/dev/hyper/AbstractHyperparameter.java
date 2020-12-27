package org.ent.dev.hyper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractHyperparameter<T> implements Hyperparameter<T> {

	protected T value;

	protected T defaultValue;

	protected String description;

	private PropertyChangeSupport changeSupport;

	protected AbstractHyperparameter(T defaultValue, String description) {
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.description = description;
		this.changeSupport = new PropertyChangeSupport(AbstractHyperparameter.class);
	}

	protected abstract void validate(T x);

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(T value) {
		validate(value);
		T oldValue = this.value;
		this.value = value;
		changeSupport.firePropertyChange("value", oldValue, value);
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

    @Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
    	changeSupport.addPropertyChangeListener(listener);
    }
}

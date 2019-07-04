package org.ent.gui;

import java.util.ArrayList;
import java.util.List;

import org.ent.dev.hyper.HyperRegistry;
import org.ent.dev.hyper.Hyperparameter;

public class HyperRegistryImpl implements HyperRegistry {

	private List<Hyperparameter<?>> parameters;

	public HyperRegistryImpl() {
		parameters = new ArrayList<>();
	}

	@Override
	public void addHyperparameter(Hyperparameter<?> hyper) {
		parameters.add(hyper);
	}

	public List<Hyperparameter<?>> getParameters() {
		return parameters;
	}

}

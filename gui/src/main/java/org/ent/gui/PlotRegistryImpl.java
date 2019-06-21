package org.ent.gui;

import org.ent.dev.stat.BinaryStats;
import org.ent.dev.stat.PlotRegistry;

public class PlotRegistryImpl implements PlotRegistry {

	public BinaryStats plot1;

	@Override
	public void addFractionPlot(BinaryStats stats) {
		this.plot1 = stats;
	}

}

package org.ent.gui;

import java.util.ArrayList;
import java.util.List;

import org.ent.dev.stat.BinnedStats;
import org.ent.dev.stat.PlotRegistry;

public class PlotRegistryImpl implements PlotRegistry {

	public List<BinnedStats> plots = new ArrayList<>();

	@Override
	public void addFractionPlot(BinnedStats stats) {
		plots.add(stats);
	}

}

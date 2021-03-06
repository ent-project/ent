package org.ent.gui;

import java.util.ArrayList;
import java.util.List;

import org.ent.dev.stat.PlotInfo;
import org.ent.dev.stat.PlotRegistry;

public class PlotRegistryImpl implements PlotRegistry {

	private final List<PlotInfo> plots = new ArrayList<>();

	@Override
	public void addPlot(PlotInfo plotInfo) {
		plots.add(plotInfo);
	}

	public List<PlotInfo> getPlots() {
		return plots;
	}
}

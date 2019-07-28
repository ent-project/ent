package org.ent.dev.stat;

import java.awt.Color;

public class PlotInfo {

	private final String id;

	private BinnedStats stats;

	private String title;

	private Double rangeMax;

	private String domainAxisLabel;

	private String rangeAxisLabel;

	private String subplotOf;

	private Color color;

	public PlotInfo(String id) {
		this.id = id;
	}

	public BinnedStats getStats() {
		return stats;
	}

	public void setStats(BinnedStats stats) {
		this.stats = stats;
	}

	public PlotInfo withStats(BinnedStats stats) {
		setStats(stats);
		return this;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public PlotInfo withTitle(String title) {
		setTitle(title);
		return this;
	}

	public Double getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(Double rangeMax) {
		this.rangeMax = rangeMax;
	}

	public PlotInfo withRangeMax(Double rangeMax) {
		setRangeMax(rangeMax);
		return this;
	}

	public String getDomainAxisLabel() {
		return domainAxisLabel;
	}

	public void setDomainAxisLabel(String domainAxisLabel) {
		this.domainAxisLabel = domainAxisLabel;
	}

	public PlotInfo withDomainAxisLabel(String domainAxisLabel) {
		setDomainAxisLabel(domainAxisLabel);
		return this;
	}

	public String getRangeAxisLabel() {
		return rangeAxisLabel;
	}

	public void setRangeAxisLabel(String rangeAxisLabel) {
		this.rangeAxisLabel = rangeAxisLabel;
	}

	public PlotInfo withRangeAxisLabel(String rangeAxisLabel) {
		setRangeAxisLabel(rangeAxisLabel);
		return this;
	}

	public String getSubplotOf() {
		return subplotOf;
	}

	public void setSubplotOf(String subplotOf) {
		this.subplotOf = subplotOf;
	}

	public PlotInfo withSubplotOf(String subplotOf) {
		setSubplotOf(subplotOf);
		return this;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public PlotInfo withColor(Color color) {
		setColor(color);
		return this;
	}

}

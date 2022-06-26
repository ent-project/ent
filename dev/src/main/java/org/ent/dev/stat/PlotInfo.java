package org.ent.dev.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlotInfo {

	private final String id;

	private List<PlotRow> rows;

	private String title;

	private Double rangeMax;

	private String domainAxisLabel;

	private String rangeAxisLabel;

	public PlotInfo(String id) {
		this.id = id;
		this.rows = new ArrayList<>();
	}

	public List<PlotRow> getRows() {
		return rows;
	}

	public void setRows(List<PlotRow> rows) {
		this.rows = rows;
	}

	public PlotInfo addRow(BinnedStat data) {
		this.rows.add(new PlotRow().withStat(data));
		return this;
	}

	public PlotInfo addRow(Consumer<PlotRow> plotRowConsumer) {
		PlotRow row = new PlotRow();
		plotRowConsumer.accept(row);
		this.rows.add(row);
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
}

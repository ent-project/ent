package org.ent.gui;

import org.ent.dev.DevelopmentPlan;
import org.ent.dev.DevelopmentPlan.RoundListener;
import org.ent.dev.stat.PlotInfo;
import org.ent.dev.stat.PlotRow;
import org.ent.dev.unit.data.Data;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class PlotDialog extends JDialog implements RoundListener {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final int MIN_PLOT_SIZE = 40;

	private volatile boolean updateRequired;

	private transient Timer updatePlotTimer;

	private static class Plot {

	    private JFreeChart chart;

	    private final JPanel chartPanel;

	    public Plot(PlotInfo plotInfo) {
			NumberAxis domainAxis = new NumberAxis(plotInfo.getDomainAxisLabel());
			domainAxis.setAutoRangeIncludesZero(false);
			ValueAxis rangeAxis = new NumberAxis(plotInfo.getRangeAxisLabel());
			if (plotInfo.getRangeMax() != null) {
				rangeAxis.setRange(new Range(0, plotInfo.getRangeMax()), true, false);
			}
			XYPlot xyPlot = new XYPlot(null, domainAxis, rangeAxis, null);
			boolean createLegend = plotInfo.getRows().stream().anyMatch(row -> row.getLabel() != null);
			this.chart = new JFreeChart(plotInfo.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, xyPlot, createLegend);
			ChartFactory.getChartTheme().apply(chart);
			xyPlot.setBackgroundPaint(new Color(233, 233, 233));

			int i = 0;
			for (List<PlotRow> rows : collectGroupedRows(plotInfo).values()) {
				xyPlot.setDataset(i, buildDataSet(rows));
				xyPlot.setRenderer(i, buildRenderer(rows));
				i++;
			}
			this.chartPanel = new ChartPanel(chart);
	    }

		private Map<String, List<PlotRow>> collectGroupedRows(PlotInfo plotInfo) {
			int i = 0;
			for (PlotRow row : plotInfo.getRows()) {
				if (row.getGroup() == null) {
					row.setGroup("internal-" + i);
					i++;
				}
			}
			return plotInfo.getRows().stream().collect(Collectors.groupingBy(PlotRow::getGroup));
		}

		private BinnedStatDataSet buildDataSet(List<PlotRow> rows) {
			BinnedStatDataSet dataset = new BinnedStatDataSet(rows.stream().map(PlotRow::getStat).toList());
			if (rows.stream().anyMatch(row -> row.getLabel() != null)) {
				dataset.setSeriesKeys(rows.stream().map(row -> row.getLabel() != null ? row.getLabel() : "").toList());
			}
			return dataset;
		}

		private XYItemRenderer buildRenderer(List<PlotRow> rows) {
			if (rows.size() == 1) {
				PlotRow row = rows.get(0);
				return switch (row.getType()) {
					case BAR -> {
						XYBarRenderer renderer = new XYBarRenderer();
						if (row.getColor() != null) {
							renderer.setSeriesPaint(0, row.getColor());
						}
						renderer.setShadowVisible(false);
						renderer.setBarPainter(new StandardXYBarPainter());
						yield renderer;
					}
					case LINE -> {
						XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
						if (row.getColor() != null) {
							lineRenderer.setSeriesPaint(0, row.getColor());
						}
						lineRenderer.setSeriesStroke(0, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
						yield  lineRenderer;
					}
				};
			} else {
				StackedXYBarRenderer stackedRenderer = new StackedXYBarRenderer(0.05);
				stackedRenderer.setDrawBarOutline(false);
				stackedRenderer.setShadowVisible(false);
				stackedRenderer.setBarPainter(new StandardXYBarPainter());
				for (int i = 0; i < rows.size(); i++) {
					PlotRow row = rows.get(i);
					if (row.getColor() != null) {
						stackedRenderer.setSeriesPaint(i, row.getColor());
					}
				}
				return stackedRenderer;
			}
		}

		public void update() {
	    	XYPlot xyPlot = chart.getXYPlot();
			BinnedStatDataSet dataset = (BinnedStatDataSet) xyPlot.getDataset();
			xyPlot.getDomainAxis().setRange(new Range(dataset.getStartX(0, dataset.getFirstItemDisplayed()),
	    			dataset.getEndX(0, dataset.getFirstItemDisplayed() + dataset.getNumBinsDisplayed() - 1)),
	    			true, false);
	    	chart.fireChartChanged();
		}
	}

	private final transient List<Plot> plots;

	public PlotDialog(Frame parent, DevelopmentPlan plan) {
		super(parent);
		setTitle("Plots");
		this.updatePlotTimer = new Timer("plotUpdate", true);
		this.updateRequired = true;
		plan.setRoundListener(this);
		this.plots = new ArrayList<>();

		build();
		pack();

		TimerTask updateTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (updateRequired) {
					updateRequired = false;
					EventQueue.invokeLater(() -> updatePlots());
				}
			}
		};
		updatePlotTimer.schedule(updateTimerTask, 0L, 100L);
	}

	private void build() {
		for (PlotInfo plotInfo : Main.getPlotRegistry().getPlots()) {
			plots.add(new Plot(plotInfo));
		}

		GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);


        ParallelGroup horGroup = layout.createParallelGroup();
        plots.forEach(plot -> horGroup.addGroup(
    		layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(plot.chartPanel, MIN_PLOT_SIZE, 512, Short.MAX_VALUE)
				.addContainerGap()
			)
		);
		layout.setHorizontalGroup(horGroup);

		SequentialGroup vertGroup = layout.createSequentialGroup().addContainerGap();
		plots.forEach(plot ->
			vertGroup.addComponent(plot.chartPanel, MIN_PLOT_SIZE, 412, Short.MAX_VALUE)
		);
		vertGroup.addContainerGap();

		layout.setVerticalGroup(vertGroup);
	}

	@Override
	public void roundCompleted(Data data) {
		updateRequired = true;
	}

    private void updatePlots() {
    	plots.forEach(Plot::update);
    }

}

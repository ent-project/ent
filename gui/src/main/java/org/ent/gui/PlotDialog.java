package org.ent.gui;

import org.ent.dev.DevelopmentPlan;
import org.ent.dev.DevelopmentPlan.RoundListener;
import org.ent.dev.stat.BinnedStat;
import org.ent.dev.stat.PlotInfo;
import org.ent.dev.stat.PlotRow;
import org.ent.dev.unit.data.Data;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
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
import java.util.Timer;
import java.util.TimerTask;

public class PlotDialog extends JDialog implements RoundListener {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final int MIN_PLOT_SIZE = 40;

	private volatile boolean updateRequired;

	private transient Timer updatePlotTimer;

	private static class Plot {

		private final PlotInfo plotInfo;

		private PlotInfo plotInfoSecondary;

	    private final BinnedStatDataSet dataset;

	    private BinnedStatDataSet datasetSecondary;

	    private JFreeChart chart;

	    private final JPanel chartPanel;

	    public Plot(PlotInfo plotInfo) {
	    	this.plotInfo = plotInfo;
			this.dataset = new BinnedStatDataSet(plotInfo.getRows().stream().map(PlotRow::getStat).toList());
			initializeChart();
			chartPanel = new ChartPanel(chart);
	    }

		public void initializeChart() {
			if (dataset.getSeriesCount() == 1) {
				initializeChartSingleData();
			} else {
				initializeChartMultiData();
			}
		}

		private void initializeChartMultiData() {
			dataset.setSeriesKeys(plotInfo.getRows().stream().map(row -> row.getLabel() != null ? row.getLabel() : "").toList());

			NumberAxis domainAxis = new NumberAxis(plotInfo.getDomainAxisLabel());
			domainAxis.setAutoRangeIncludesZero(false);
			ValueAxis rangeAxis = new NumberAxis(plotInfo.getRangeAxisLabel());
			if (plotInfo.getRangeMax() != null) {
				rangeAxis.setRange(new Range(0, plotInfo.getRangeMax()), true, false);
			}
			StackedXYBarRenderer renderer = new StackedXYBarRenderer(0.05);

			XYPlot xyPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
			chart = new JFreeChart(plotInfo.getTitle(), xyPlot);
			ChartFactory.getChartTheme().apply(chart);

			xyPlot.setBackgroundPaint(new Color(233, 233, 233));
			renderer.setDrawBarOutline(false);
			renderer.setShadowVisible(false);
			StandardXYBarPainter painter = new StandardXYBarPainter();
			renderer.setBarPainter(painter);
			for (int i = 0; i < plotInfo.getRows().size(); i++) {
				PlotRow row = plotInfo.getRows().get(i);
				if (row.getColor() != null) {
					renderer.setSeriesPaint(i, row.getColor());
				}
			}
		}

		private void initializeChartSingleData() {
			chart = ChartFactory.createXYBarChart(
					plotInfo.getTitle(),
					plotInfo.getDomainAxisLabel(),
					false,
					plotInfo.getRangeAxisLabel(),
					dataset, PlotOrientation.VERTICAL, false, true, false);
			XYPlot xyPlot = chart.getXYPlot();
			if (plotInfo.getRangeMax() != null) {
				xyPlot.getRangeAxis().setRange(new Range(0, plotInfo.getRangeMax()), true, false);
			}
			xyPlot.setBackgroundPaint(new Color(233, 233, 233));
			XYBarRenderer renderer = (XYBarRenderer) xyPlot.getRenderer();
			if (plotInfo.getRows().get(0).getColor() != null) {
				renderer.setSeriesPaint(0, plotInfo.getRows().get(0).getColor());
			}
			StandardXYBarPainter painter = new StandardXYBarPainter();
			renderer.setBarPainter(painter);
		}

		public void addSecondaryPlot(PlotInfo plotInfoSecondary) {
			this.plotInfoSecondary = plotInfoSecondary;
	    	BinnedStat statsSecondary = plotInfoSecondary.getRows().get(0).getStat();
			this.datasetSecondary = new BinnedStatDataSet(statsSecondary);

			XYPlot xyPlot = chart.getXYPlot();
			xyPlot.setDataset(1, datasetSecondary);
			XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
			if (plotInfoSecondary.getRows().get(0).getColor() != null) {
				renderer2.setSeriesPaint(0, plotInfoSecondary.getRows().get(0).getColor());
			}
			renderer2.setSeriesStroke(0, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			xyPlot.setRenderer(1, renderer2);
			xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		}

		public void update() {
	    	XYPlot xyPlot = chart.getXYPlot();
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
			if (plotInfo.getSubplotOf() == null) {
				plots.add(new Plot(plotInfo));
			}
		}
		for (PlotInfo plotInfo : Main.getPlotRegistry().getPlots()) {
			String subplotOf = plotInfo.getSubplotOf();
			if (subplotOf != null) {
				Plot parentPlot = plots.stream().filter(p -> p.plotInfo.getId().equals(subplotOf)).findAny()
						.orElseThrow(() -> new AssertionError("Parent plot %s not found".formatted(subplotOf)));
				parentPlot.addSecondaryPlot(plotInfo);
			}
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

package org.ent.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.ent.dev.DevelopmentPlan;
import org.ent.dev.DevelopmentPlan.RoundListener;
import org.ent.dev.stat.BinnedStats;
import org.ent.dev.stat.PlotInfo;
import org.ent.dev.unit.Data;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;

public class PlotDialog extends JDialog implements RoundListener {

	private static final long serialVersionUID = 1L;

	private static final int MIN_PLOT_SIZE = 40;

	private volatile boolean updateRequired;

	private Timer updatePlotTimer;

	private static class Plot {

		private PlotInfo plotInfo;

		private PlotInfo plotInfoSecondary;

	    private BinnedStatsDataSet dataset;

	    private BinnedStatsDataSet datasetSecondary;

	    private JFreeChart chart;

	    private JPanel chartPanel;

	    public Plot(PlotInfo plotInfo) {
	    	this.plotInfo = plotInfo;
			this.dataset = new BinnedStatsDataSet(plotInfo.getStats());
			initializeChart();
			chartPanel = new ChartPanel(chart);
	    }

		public void initializeChart() {
			chart = ChartFactory.createXYBarChart(
					plotInfo.getTitle(),
					plotInfo.getDomainAxisLabel(),
					false,
					plotInfo.getRangeAxisLabel(),
					dataset);
			chart.removeLegend();
			XYPlot xyPlot = chart.getXYPlot();
	    	if (plotInfo.getRangeMax() != null) {
	    		xyPlot.getRangeAxis().setRange(new Range(0, plotInfo.getRangeMax()), true, false);
	    	}
			xyPlot.setBackgroundPaint(new Color(233, 233, 233));
			XYBarRenderer renderer = (XYBarRenderer) xyPlot.getRenderer();
			if (plotInfo.getColor() != null) {
				renderer.setSeriesPaint(0, plotInfo.getColor());
			}
			StandardXYBarPainter painter = new StandardXYBarPainter();
			renderer.setBarPainter(painter);
		}

		public void addSecondaryPlot(PlotInfo plotInfoSecondary) {
			this.plotInfoSecondary = plotInfoSecondary;
	    	BinnedStats statsSecondary = plotInfoSecondary.getStats();
			this.datasetSecondary = new BinnedStatsDataSet(statsSecondary);

			XYPlot xyPlot = chart.getXYPlot();
			xyPlot.setDataset(1, datasetSecondary);
			XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
			if (plotInfoSecondary.getColor() != null) {
				renderer2.setSeriesPaint(0, plotInfoSecondary.getColor());
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

	private List<Plot> plots;

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
					EventQueue.invokeLater(() -> {
						updatePlots();
					});
				}
			}
		};
		updatePlotTimer.schedule(updateTimerTask, 0L, 100L);
	}

	private void build() {
		for (PlotInfo plotInfo : Main.getPlotRegistry().plots) {
			if (plotInfo.getSubplotOf() == null) {
				plots.add(new Plot(plotInfo));
			}
		}
		for (PlotInfo plotInfo : Main.getPlotRegistry().plots) {
			String subplotOf = plotInfo.getSubplotOf();
			if (subplotOf != null) {
				Plot parentPlot = plots.stream().filter(p -> p.plotInfo.getId().equals(subplotOf)).findAny().get();
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

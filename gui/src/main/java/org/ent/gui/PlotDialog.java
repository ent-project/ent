package org.ent.gui;

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
import org.ent.dev.unit.Data;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.Range;

public class PlotDialog extends JDialog implements RoundListener {

	private static final long serialVersionUID = 1L;

	private static final int MIN_PLOT_SIZE = 40;

	private volatile boolean updateRequired;

	private Timer updatePlotTimer;

	private static class Plot {

		private BinnedStats stats;

	    private BinnedStatsDataSet dataset;

	    private JFreeChart chart;

	    private JPanel chartPanel;

	    public Plot(BinnedStats stats) {
			this.stats = stats;
			this.dataset = new BinnedStatsDataSet(stats);
			initializeChart();
			chartPanel = new ChartPanel(chart);
	    }

		public void initializeChart() {
			chart = ChartFactory.createXYBarChart(stats.getTitle(), null, false, null,
					dataset);
			chart.removeLegend();
			XYPlot xyPlot = chart.getXYPlot();
	    	xyPlot.getRangeAxis().setRange(new Range(0, 0.1), true, false);
			xyPlot.setBackgroundPaint(new Color(233, 233, 233));
			XYBarRenderer renderer = (XYBarRenderer) xyPlot.getRenderer();
			StandardXYBarPainter painter = new StandardXYBarPainter();
			renderer.setBarPainter(painter);
		}

		public void update() {
	    	XYPlot xyPlot = chart.getXYPlot();
	    	xyPlot.getDomainAxis().setRange(new Range(dataset.getStartX(0, 0), dataset.getEndX(0, dataset.getItemCount(0))), true, false);
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

		for (BinnedStats stat : Main.getPlotRegistry().plots) {
			Plot plot = new Plot(stat);
			plots.add(plot);
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

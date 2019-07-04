package org.ent.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.ent.dev.DevelopmentPlan;
import org.ent.dev.DevelopmentPlan.RoundListener;
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

    private JFreeChart chart;

    private BinaryStatsDataSet dataset;

	public PlotDialog(Frame parent, DevelopmentPlan plan) {
		super(parent);
		setTitle("Plots");
		this.updatePlotTimer = new Timer("plotUpdate", true);
		this.updateRequired = true;
		plan.setRoundListener(this);

		build();
		pack();

		TimerTask updateTimerTask = new TimerTask() {
			@Override
			public void run() {
				if (updateRequired) {
					updateRequired = false;
					EventQueue.invokeLater(() -> {
						updatePlot();
					});
				}
			}
		};
		updatePlotTimer.schedule(updateTimerTask, 0L, 100L);
	}

	private void build() {
        JPanel chartPanel = buildChartPanel();

		GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup()
        		.addGroup(layout.createSequentialGroup()
        				.addContainerGap()
        				.addComponent(chartPanel, MIN_PLOT_SIZE, 512, Short.MAX_VALUE)
        				.addContainerGap()
        				)
				);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(chartPanel, MIN_PLOT_SIZE, 412, Short.MAX_VALUE)
				.addContainerGap()
				);

	}

	private JPanel buildChartPanel() {
		initializeChart();
		return new ChartPanel(chart);
	}


	public void initializeChart() {
		dataset = new BinaryStatsDataSet(Main.getPlotRegistry().plot1);
		chart = ChartFactory.createXYBarChart("Fraction passing level 1", null, false, null,
				dataset);
		chart.removeLegend();
		XYPlot xyPlot = chart.getXYPlot();
    	xyPlot.getRangeAxis().setRange(new Range(0, 0.3), true, false);
		xyPlot.setBackgroundPaint(new Color(233, 233, 233));
		XYBarRenderer renderer = (XYBarRenderer) xyPlot.getRenderer();
		StandardXYBarPainter painter = new StandardXYBarPainter();
		renderer.setBarPainter(painter);
	}

	@Override
	public void roundCompleted(Data data) {
		updateRequired = true;
	}

    private void updatePlot() {
    	XYPlot xyPlot = chart.getXYPlot();
    	xyPlot.getDomainAxis().setRange(new Range(dataset.getStartX(0, 0), dataset.getEndX(0, dataset.getItemCount(0))), true, false);
    	chart.fireChartChanged();
    }


}

package org.ent.gui;

import java.awt.EventQueue;
import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

import javax.swing.JDialog;

import org.ent.dev.DevelopmentPlan;
import org.ent.gui.pref.WindowGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final PlotRegistryImpl plotRegistry = new PlotRegistryImpl();

	private static final HyperRegistryImpl hyperRegistry = new HyperRegistryImpl();

	private static DevelopmentPlan plan;

	private static MainFrame mainFrame;

	private static JDialog plotDialog;

	private static ParametersDialog parametersDialog;

	private static PropertyChangeSupport plotDialogCreated = new PropertyChangeSupport(Main.class);

	public static void main(String[] args) {
		plan = new DevelopmentPlan(plotRegistry, hyperRegistry);
		EventQueue.invokeLater(() -> {
				mainFrame = new MainFrame(plan);
				mainFrame.setLocationRelativeTo(null);
				new WindowGeometry(mainFrame, "main").restore().monitor();
				mainFrame.setVisible(true);
			}
		);
	}

	public static JDialog getPlotDialog() {
		if (plotDialog == null) {
			plotDialog = new PlotDialog(mainFrame, plan);
			new WindowGeometry(plotDialog, "plot").restore().monitor();
			plotDialogCreated.firePropertyChange("plotDialog", null, plotDialog);
		}
		return plotDialog;
	}

	public static void addPlotDialogCreatedListener(Consumer<JDialog> listener) {
		plotDialogCreated.addPropertyChangeListener(pcl -> listener.accept((JDialog) pcl.getNewValue()));
	}

	public static ParametersDialog getParametersDialog() {
		if (parametersDialog == null) {
			parametersDialog = new ParametersDialog(mainFrame);
		}
		return parametersDialog;
	}

	public static PlotRegistryImpl getPlotRegistry() {
		return plotRegistry;
	}

	public static HyperRegistryImpl getHyperRegistry() {
		return hyperRegistry;
	}
}

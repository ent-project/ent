package org.ent.gui;

import java.awt.EventQueue;
import java.beans.PropertyChangeSupport;

import javax.swing.JDialog;

import org.ent.dev.DevelopmentPlan;
import org.ent.gui.pref.WindowGeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static final String PLOT_DIALOG = "plotDialog";

	public static final String PARAMETERS_DIALOG = "parametersDialog";

	private static final PlotRegistryImpl plotRegistry = new PlotRegistryImpl();

	private static final HyperRegistryImpl hyperRegistry = new HyperRegistryImpl();

	private static DevelopmentPlan plan;

	private static MainFrame mainFrame;

	private static JDialog plotDialog;

	private static ParametersDialog parametersDialog;

	private static PropertyChangeSupport dialogCreated = new PropertyChangeSupport(Main.class);

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
			dialogCreated.firePropertyChange(PLOT_DIALOG, null, plotDialog);
		}
		return plotDialog;
	}

	public static void addDialogCreatedListener(DialogCreatedListener listener) {
		dialogCreated.addPropertyChangeListener(pcl -> {
			listener.dialogCreated(pcl.getPropertyName(), (JDialog) pcl.getNewValue());
		});
	}

	public static ParametersDialog getParametersDialog() {
		if (parametersDialog == null) {
			parametersDialog = new ParametersDialog(mainFrame);
			new WindowGeometry(parametersDialog, "parameters")
				.withManageSize(false)
				.restore()
				.monitor();
			dialogCreated.firePropertyChange(PARAMETERS_DIALOG, null, parametersDialog);
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

package org.ent.gui;

import java.awt.EventQueue;
import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

import javax.swing.JDialog;

import org.ent.dev.DevelopmentPlan;
import org.ent.gui.pref.WindowGeometry;

public class Main {

	private static DevelopmentPlan plan;

	private static JDialog plotDialog;

	private static PropertyChangeSupport plotDialogCreated = new PropertyChangeSupport(Main.class);

	private static PlotRegistryImpl plotRegistry = new PlotRegistryImpl();

	public static void main(String[] args) {
		plan = new DevelopmentPlan(plotRegistry);
		EventQueue.invokeLater(() -> {
				MainFrame mainFrame = new MainFrame(plan);
				mainFrame.setLocationRelativeTo(null);
				new WindowGeometry(mainFrame, "main").restore().monitor();
				mainFrame.setVisible(true);
			}
		);
	}

	public static JDialog getPlotDialog() {
		if (plotDialog == null) {
			plotDialog = new PlotDialog(plan);
			new WindowGeometry(plotDialog, "plot").restore().monitor();
			plotDialogCreated.firePropertyChange("plotDialog", null, plotDialog);
		}
		return plotDialog;
	}

	public static void addPlotDialogCreatedListener(Consumer<JDialog> listener) {
		plotDialogCreated.addPropertyChangeListener(pcl -> listener.accept((JDialog) pcl.getNewValue()));
	}

	public static PlotRegistryImpl getPlotRegistry() {
		return plotRegistry;
	}
}

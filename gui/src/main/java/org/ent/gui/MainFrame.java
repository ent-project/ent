package org.ent.gui;

import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.ent.dev.DevelopmentPlan;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private DevelopmentPlan plan;

	private ExecutorService executor;

	private JToggleButton btnPlots;
	private JToggleButton btnParameters;

	public MainFrame(DevelopmentPlan plan) throws HeadlessException {
		super("Ent");

		this.plan = plan;
		this.executor = Executors.newFixedThreadPool(1);
		Main.addDialogCreatedListener((String dialogName, JDialog dialog) ->
			{
				JToggleButton button;
				switch (dialogName) {
				case Main.PLOT_DIALOG:
					button = btnPlots;
					break;
				case Main.PARAMETERS_DIALOG:
					button = btnParameters;
					break;
				default:
					throw new AssertionError("unexpected dialogName: " + dialogName);
				}
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {

						button.setSelected(false);
					}
				});
			}
		);

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		build();
		pack();
	}

	private class PlayAction implements ActionListener {

		private final int batchSize;
		private final String label;

		public PlayAction(int batchSize, String label) {
			this.batchSize = batchSize;
			this.label = label;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			executor.execute(this::executePlan);
		}

		public void executePlan() {
			plan.executeBatch(batchSize);
		}

		public String getLabel() {
			return label;
		}
	}

	private void build() {

		JToolBar toolbarMain = new JToolBar();
		toolbarMain.setFloatable(false);
		toolbarMain.setRollover(true);
		toolbarMain.setBorderPainted(false);

		btnPlots = new JToggleButton();
		btnPlots.addActionListener(this::togglePlotDialog);
		btnPlots.setIcon(new ImageIcon(getClass().getResource("/icons/plot.png")));
		toolbarMain.add(btnPlots);

		btnParameters = new JToggleButton();
		btnParameters.addActionListener(this::toggleParametersDialog);
		btnParameters.setIcon(new ImageIcon(getClass().getResource("/icons/parameters.png")));
		toolbarMain.add(btnParameters);

		JToolBar toolbarCtrl = new JToolBar();
		toolbarCtrl.setFloatable(false);
		toolbarCtrl.setRollover(true);
		toolbarCtrl.setBorderPainted(false);

		List<PlayAction> playActions = Arrays.asList(
				new PlayAction(1, "1"),
				new PlayAction(10, "10"),
				new PlayAction(100, "100"),
				new PlayAction(1000, "1k"),
				new PlayAction(10000, "10k"),
				new PlayAction(100000, "100k"),
				new PlayAction(1000000, "1M"),
				new PlayAction(-1, "∞")
				);

		for (PlayAction playAction : playActions) {
			JButton btnPlay = new JButton();
			btnPlay.setText(playAction.getLabel());
			btnPlay.setIcon(new ImageIcon(getClass().getResource("/icons/start.png")));
			btnPlay.addActionListener(playAction);
			styleToolBarButton(btnPlay);
			toolbarCtrl.add(btnPlay);
		}

		JButton btnStop = new JButton();
		btnStop.setText(" ");
		btnStop.setIcon(new ImageIcon(getClass().getResource("/icons/stop.png")));
		btnStop.addActionListener(this::stop);
		styleToolBarButton(btnStop);
		toolbarCtrl.add(btnStop);

		JButton btnStats = new JButton();
		btnStats.setText(" ");
		btnStats.setIcon(new ImageIcon(getClass().getResource("/icons/stats.png")));
		btnStats.addActionListener(this::stats);
		styleToolBarButton(btnStats);
		toolbarCtrl.add(btnStats);

		GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(toolbarMain)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(toolbarCtrl)
						.addContainerGap())
				);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(toolbarMain)
				.addPreferredGap(UNRELATED)
				.addComponent(toolbarCtrl)
				.addContainerGap()
				);
	}

	private void styleToolBarButton(JButton toolBarButton) {
		toolBarButton.setFocusable(false);
		toolBarButton.setHorizontalTextPosition(SwingConstants.CENTER);
		toolBarButton.setVerticalTextPosition(SwingConstants.BOTTOM);
	}

    private void stop(ActionEvent evt) {
		plan.stop();
    }

    private void stats(ActionEvent evt) {
    	plan.dumpStats();
    }

	private void togglePlotDialog(ActionEvent evt) {
		boolean visible = btnPlots.isSelected();
		JDialog plotDialog = Main.getPlotDialog();
		if (visible && !plotDialog.isVisible()) {
			plotDialog.setVisible(true);
		} else if (!visible && plotDialog.isVisible()) {
			plotDialog.setVisible(false);
		}
	}

	private void toggleParametersDialog(ActionEvent evt) {
		boolean visible = btnParameters.isSelected();
		ParametersDialog parametersDialog = Main.getParametersDialog();
		if (visible && !parametersDialog.isVisible()) {
			parametersDialog.setVisible(true);
		} else if (!visible && parametersDialog.isVisible()) {
			parametersDialog.setVisible(false);
		}
	}
}

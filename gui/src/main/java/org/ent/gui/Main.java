package org.ent.gui;

import java.awt.EventQueue;

import org.ent.gui.pref.WindowGeometry;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
				MainFrame mainFrame = new MainFrame();
				mainFrame.setLocationRelativeTo(null);
				new WindowGeometry(mainFrame, "main").restore().monitor();
				mainFrame.setVisible(true);
			}
		);
	}
}

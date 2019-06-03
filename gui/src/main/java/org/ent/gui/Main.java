package org.ent.gui;

import java.awt.EventQueue;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
				MainFrame mainFrame = new MainFrame();
				mainFrame.setLocationRelativeTo(null);
				mainFrame.setVisible(true);
			}
		);
	}
}

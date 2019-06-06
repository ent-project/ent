package org.ent.gui.pref;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

public class WindowGeometry {

	private JFrame frame;

	private String preferenceKey;

	public WindowGeometry(JFrame frame, String name) {
		this.frame = frame;
		this.preferenceKey = "gui." + name + ".geometry";
	}

	public WindowGeometry restore() {
		Preferences pref = Preferences.userRoot();
		int width = pref.getInt(preferenceKey + ".width", -1);
		int height = pref.getInt(preferenceKey + ".height", -1);
		int x = pref.getInt(preferenceKey + ".x", -1);
		int y = pref.getInt(preferenceKey + ".y", -1);

		if (width != -1) {
			frame.setLocation(x, y);
			frame.setSize(width, height);
		}
		return this;
	}

	public void monitor() {
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				save();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				save();
			}
		});
	}

	private void save() {
		Point location = frame.getLocation();
		Dimension size = frame.getSize();

		Preferences pref = Preferences.userRoot();
		pref.putInt(preferenceKey + ".width", size.width);
		pref.putInt(preferenceKey + ".height", size.height);
		pref.putInt(preferenceKey + ".x", location.x);
		pref.putInt(preferenceKey + ".y", location.y);
	}

}

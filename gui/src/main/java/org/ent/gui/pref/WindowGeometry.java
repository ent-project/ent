package org.ent.gui.pref;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

public class WindowGeometry {

	private Window window;

	private String preferenceKey;

	public WindowGeometry(Window window, String name) {
		this.window = window;
		this.preferenceKey = "gui." + name + ".geometry";
	}

	public WindowGeometry restore() {
		Preferences pref = Preferences.userRoot();
		int width = pref.getInt(preferenceKey + ".width", -1);
		int height = pref.getInt(preferenceKey + ".height", -1);
		int x = pref.getInt(preferenceKey + ".x", -1);
		int y = pref.getInt(preferenceKey + ".y", -1);

		if (width != -1) {
			window.setLocation(x, y);
			window.setSize(width, height);
		}
		return this;
	}

	public void monitor() {
		window.addComponentListener(new ComponentAdapter() {
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
		Point location = window.getLocation();
		Dimension size = window.getSize();

		Preferences pref = Preferences.userRoot();
		pref.putInt(preferenceKey + ".width", size.width);
		pref.putInt(preferenceKey + ".height", size.height);
		pref.putInt(preferenceKey + ".x", location.x);
		pref.putInt(preferenceKey + ".y", location.y);
	}

}

package org.ent.gui.pref;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

public class WindowGeometry {

	private static final int UNINITIALIZED = -99999;

	private Window window;

	private String preferenceKey;

	private boolean managePosition;

	private boolean manageSize;

	public WindowGeometry(Window window, String name) {
		this.window = window;
		this.preferenceKey = "gui." + name + ".geometry";
		this.managePosition = true;
		this.manageSize = true;
	}

	public boolean isManagePosition() {
		return managePosition;
	}

	public void setManagePosition(boolean managePosition) {
		this.managePosition = managePosition;
	}

	public WindowGeometry withManagePosition(boolean managePosition) {
		setManagePosition(managePosition);
		return this;
	}

	public boolean isManageSize() {
		return manageSize;
	}

	public void setManageSize(boolean manageSize) {
		this.manageSize = manageSize;
	}

	public WindowGeometry withManageSize(boolean manageSize) {
		setManageSize(manageSize);
		return this;
	}

	public WindowGeometry restore() {
		Preferences pref = Preferences.userRoot();
		if (managePosition) {
			int x = pref.getInt(preferenceKey + ".x", UNINITIALIZED);
			int y = pref.getInt(preferenceKey + ".y", UNINITIALIZED);
			if (x != UNINITIALIZED) {
				window.setLocation(x, y);
			}
		}
		if (manageSize) {
			int width = pref.getInt(preferenceKey + ".width", UNINITIALIZED);
			int height = pref.getInt(preferenceKey + ".height", UNINITIALIZED);
			if (width != UNINITIALIZED) {
				window.setSize(width, height);
			}
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
		Preferences pref = Preferences.userRoot();
		if (managePosition) {
			Point location = window.getLocation();
			pref.putInt(preferenceKey + ".x", location.x);
			pref.putInt(preferenceKey + ".y", location.y);
		}
		if (manageSize) {
			Dimension size = window.getSize();
			pref.putInt(preferenceKey + ".width", size.width);
			pref.putInt(preferenceKey + ".height", size.height);
		}
	}

}

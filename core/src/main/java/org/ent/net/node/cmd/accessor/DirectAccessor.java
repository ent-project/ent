package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.Purview;

public class DirectAccessor implements Accessor {

	@Override
	public int getCode() {
		return 0b1;
	}

	@Override
	public Arrow get(Arrow arrow, Purview purview) {
		return arrow;
	}

	@Override
	public String getShortName() {
		return "";
	}

	@Override
	public String getShortNameAscii() {
		return "";
	}
}

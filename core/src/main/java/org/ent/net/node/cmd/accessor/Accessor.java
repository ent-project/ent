package org.ent.net.node.cmd.accessor;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;

public interface Accessor {

	Arrow get(Arrow arrow, Ent ent, Purview purview);

	int getCode();

	default String getShortName() {
		return getShortNameAscii();
	}

	String getShortNameAscii();
}

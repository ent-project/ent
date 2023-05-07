package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.Purview;

public interface Accessor {

	Arrow get(Arrow arrow, Purview purview);

	int getCode();

	String getShortName();

	String getShortNameAscii();
}

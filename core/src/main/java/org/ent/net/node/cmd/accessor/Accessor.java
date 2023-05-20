package org.ent.net.node.cmd.accessor;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public interface Accessor {

	Arrow get(Node base, Ent ent, Purview purview);

	int getCode();

	String getShortName();
}

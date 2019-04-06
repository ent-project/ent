package org.ent.dev.randnet;

import org.ent.net.node.cmd.Command;

public interface CommandDrawing {

	/**
	 * Return a command, usually randomly from a given selection.
	 */
	Command drawCommand();

}

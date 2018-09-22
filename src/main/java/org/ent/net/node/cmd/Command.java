package org.ent.net.node.cmd;

public interface Command {

	String getShortName();

    default String getShortNameAscii() {
        return getShortName();
    }

}

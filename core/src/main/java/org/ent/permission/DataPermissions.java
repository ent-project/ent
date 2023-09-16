package org.ent.permission;

import org.ent.net.Net;

public class DataPermissions implements Permissions {

    private final boolean[] canWriteValue;
    private final boolean[] canWriteNewNode;
    private final boolean[] canWriteArrow;
    private final boolean[] canPointTo;
    private final boolean[] canExecute;

    public DataPermissions(boolean[] canWriteValue,
                           boolean[] canWriteNewNode,
                           boolean[] canWriteArrow,
                           boolean[] canPointTo,
                           boolean[] canExecute) {
        this.canWriteValue = canWriteValue;
        this.canWriteNewNode = canWriteNewNode;
        this.canWriteArrow = canWriteArrow;
        this.canPointTo = canPointTo;
        this.canExecute = canExecute;
    }

    @Override
    public boolean noWrite(int indexTarget, WriteFacet facet) {
        return switch (facet) {
            case VALUE -> !canWriteValue[indexTarget];
            case NEW_NODE -> !canWriteNewNode[indexTarget];
            case ARROW -> !canWriteArrow[indexTarget];
        };
    }

    @Override
    public boolean noPointTo(int indexTarget) {
        return !canPointTo[indexTarget];
    }

    @Override
    public boolean noExecute(Net net) {
        if (canExecute[net.getNetIndex()]) {
            // double check to see if the net thinks it is eligible for execution
            if (!net.isExecutable()) {
                throw new PermissionsViolatedException();
            }
            return false;
        } else {
            return true;
        }
    }
}

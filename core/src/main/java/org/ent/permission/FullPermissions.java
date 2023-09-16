package org.ent.permission;

import org.ent.net.Net;

public class FullPermissions implements Permissions {

    @Override
    public boolean noWrite(int indexTarget, WriteFacet facet) {
        return false;
    }

    @Override
    public boolean noPointTo(int indexTarget) {
        return false;
    }

    @Override
    public boolean noExecute(Net net) {
        return false;
    }
}

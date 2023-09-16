package org.ent.permission;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.node.Node;

/**
 * One {@link Ent} may contain multiple domains, including the main {@link Net}.
 * Each domain is the owner of one {@link Permissions} instance, representing what
 * this domain may or may not do with respect to other domains.
 * <p>
 * In the most basic case, only the {@link Permissions} of the main {@link Net} is relevant.
 * However, you may pass execution control to another domain (eval flow operation), which makes
 * another domain the actor. For all operation in this new context, the relevant {@link Permissions}
 * are the {@link Permissions} of the actor domain.
 */
public interface Permissions {

    boolean DOUBLE_CHECK_PERMISSIONS = true;

    FullPermissions DIRECT = new FullPermissions();

    /**
     * Can the owner of the permissions modify the net {@code indexTarget}
     * in a certain aspect (given by {@code facet})?
     * <p>
     * I.e. can they in the net {@code indexTarget}
     *  - change values?
     *  - create new nodes?
     *  - point arrows of nodes to different targets?
     */
    boolean noWrite(int indexTarget, WriteFacet facet);

    default boolean noWrite(Net targetNet, WriteFacet facet) {
        return noWrite(targetNet.getNetIndex(), facet);
    }

    default boolean noWrite(Node target, WriteFacet facet) {
        return noWrite(target.getNet().getNetIndex(), facet);
    }

    default boolean noWrite(Arrow setter, WriteFacet facet) {
        return noWrite(setter.getOrigin().getNet().getNetIndex(), facet);
    }

    /**
     * Can an arrow point from the domain that owns this {@link Permissions}
     * instance to the {@code indexTarget} domain?
     * <p>
     * This is a deviation from the logic of the other permission methods:
     * Whether an arrow can point from domain A to domain B is independent
     * of the actor that tries to create the link.
     */
    boolean noPointTo(int indexTarget);

    default boolean noPointTo(Node target) {
        return noPointTo(target.getNet().getNetIndex());
    }

    /**
     * Can the owner of the permissions pass control of execution to the domain {@code indexTarget}?
     */
    boolean noExecute(Net net);

    default boolean noExecute(Node target) {
        return noExecute(target.getNet());
    }

    default boolean noExecute(Arrow handle) {
        return noExecute(handle.getTarget(Permissions.DIRECT));
    }

    default boolean shouldFireEvent() {
        // Use of DIRECT instance indicates that this is
        // done outside of ent execution (e.g. setup or post analysis)
        return this != Permissions.DIRECT;
    }
}

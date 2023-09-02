package org.ent.dev.trim;

import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class TrimmingHelper {

    public static void trim(Net net, TrimmingListener trimmingListener) {
        for (Node node : net.getNodes()) {
            for (Arrow arrow : node.getArrows()) {
                if (trimmingListener.isDead(arrow)) {
                    arrow.setTarget(arrow.getOrigin(), Purview.DIRECT);
                }
            }
        }
        net.referentialGarbageCollection();
        net.setSparse(true);
    }
}

package org.ent.dev.trim;

import org.ent.Ent;
import org.ent.dev.RunSetup;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.util.NetCopy;

/**
 * NetTrimmer simplifies a net to those nodes and arrows that are relevant to the execution.
 */
public class NetTrimmer {

	private final Ent ent;

	private final Net net;

	private final RunSetup runSetup;

	private NetCopy copy;

	private MarkerNode marker;

	private TrimmingWorker trimmingWorker;

	public NetTrimmer(Ent ent, RunSetup runSetup) {
		this.ent = ent;
		this.net = ent.getNet();
		this.runSetup = runSetup;
		if (net.isMarkerNodePermitted()) {
			this.marker = net.getMarkerNode();
		} else {
			this.marker = net.permitMarkerNode();
		}
	}

	public void runTrimmer() {
		copy = new NetCopy(net);
		copy.createCopy();
		Ent entCopy = new Ent(copy.getClonedNet()); // FIXME

		trimmingWorker = new TrimmingWorker(entCopy, runSetup);
		trimmingWorker.runTrimmer();

		cutDeadArrows();

		net.referentialGarbageCollection();
	}

	public MarkerNode getMarker() {
		return marker;
	}

	private void cutDeadArrows() {
		for (Node n : net.getNodes()) {
			for (Arrow arrow : n.getArrows()) {
				if (isDead(arrow)) {
					cutArrow(arrow);
				}
			}
		}
	}

	private boolean isDead(Arrow arrow) {
		Arrow arrowClone = copy.originalToClone(arrow);
		return trimmingWorker.isDead(arrowClone);
	}

	private void cutArrow(Arrow arrow) {
		arrow.setTarget(arrow.getOrigin(), Purview.DIRECT);
	}
}

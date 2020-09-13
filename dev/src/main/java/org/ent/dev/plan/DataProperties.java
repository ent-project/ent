package org.ent.dev.plan;

import org.ent.dev.NetReplicator;
import org.ent.dev.unit.data.Data;
import org.ent.net.Net;

public interface DataProperties {

	interface PropNet extends Data {
		String KEY = "net";

		default Net getNet() {
			return (Net) getProperty(KEY);
		}

		default void setNet(Net net) {
			setProperty(KEY, net);
		}
	}

	interface PropSeed extends Data {
		String KEY = "seed";

		default Long getSeed() {
			return (Long) getProperty(KEY);
		}

		default void setSeed(Long seed) {
			setProperty(KEY, seed);
		}
	}

	interface PropReplicator extends Data {
		String KEY = "replicator";

		default NetReplicator getReplicator() {
			return (NetReplicator) getProperty(KEY);
		}

		default void setReplicator(NetReplicator replicator) {
			setProperty(KEY, replicator);
		}
	}

	interface PropSerialNumber extends Data {
		String KEY = "serialNumber";

		default Long getSerialNumber() {
			return (Long) getProperty(KEY);
		}

		default void setSerialNumber(Long serialNumber) {
			setProperty(KEY, serialNumber);
		}
	}

	interface PropStepsExamResult extends Data {
		String KEY = "stepsExamResult";

		default StepsExamResult getStepsExamResult() {
			return (StepsExamResult) getProperty(KEY);
		}

		default void setStepsExamResult(StepsExamResult stepsExamResult) {
			setProperty(KEY, stepsExamResult);
		}
	}

}

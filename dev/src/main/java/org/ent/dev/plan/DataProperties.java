package org.ent.dev.plan;

import org.ent.dev.NetReplicator;
import org.ent.dev.unit.data.Data;
import org.ent.net.Net;

public interface DataProperties {

	public interface PropNet extends Data {
		static final String KEY = "net";

		default Net getNet() {
			return (Net) getProperty(KEY);
		}

		default void setNet(Net net) {
			setProperty(KEY, net);
		}
	}

	public interface PropSeed extends Data {
		static final String KEY = "seed";

		default Long getSeed() {
			return (Long) getProperty(KEY);
		}

		default void setSeed(Long seed) {
			setProperty(KEY, seed);
		}
	}

	public interface PropReplicator extends Data {
		static final String KEY = "replicator";

		default NetReplicator getReplicator() {
			return (NetReplicator) getProperty(KEY);
		}

		default void setReplicator(NetReplicator replicator) {
			setProperty(KEY, replicator);
		}
	}

	public interface PropSerialNumber extends Data {
		static final String KEY = "serialNumber";

		default Long getSerialNumber() {
			return (Long) getProperty(KEY);
		}

		default void setSerialNumber(Long serialNumber) {
			setProperty(KEY, serialNumber);
		}
	}

	public interface PropStepsExamResult extends Data {
		static final String KEY = "stepsExamResult";

		default StepsExamResult getStepsExamResult() {
			return (StepsExamResult) getProperty(KEY);
		}

		default void setStepsExamResult(StepsExamResult stepsExamResult) {
			setProperty(KEY, stepsExamResult);
		}
	}

	public interface PropSourceInfo extends Data {
		static final String KEY = "sourceInfo";

		default String getSourceInfo() {
			return (String) getProperty(KEY);
		}

		default void setSourceInfo(String sourceInfo) {
			setProperty(KEY, sourceInfo);
		}
	}

}

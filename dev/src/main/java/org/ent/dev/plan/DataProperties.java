package org.ent.dev.plan;

import org.ent.Ent;
import org.ent.dev.NetReplicator;
import org.ent.dev.unit.data.Data;

public interface DataProperties {

	interface PropEnt extends Data {
		String KEY = "ent";

		default Ent getEnt() {
			return (Ent) getProperty(KEY);
		}

		default void setEnt(Ent ent) {
			setProperty(KEY, ent);
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

	interface PropVariabilityExamResult extends Data {
		String KEY = "variabilityExamResult";

		default VariabilityExamResult getVariabilityExamResult() {
			return (VariabilityExamResult) getProperty(KEY);
		}

		default void setVariabilityExamResult(VariabilityExamResult variabilityExamResult) {
			setProperty(KEY, variabilityExamResult);
		}
	}
}

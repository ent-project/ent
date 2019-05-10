package org.ent.dev.plan;

import java.util.Map;

import org.ent.dev.NetReplicator;
import org.ent.dev.StepsExam.StepsExamResult;
import org.ent.net.Net;

public interface Data {

	Object getProperty(String key);

	void setProperty(String key, Object value);

	Map<String, Object> getProperties();


	public interface PropNet extends Data {
		static final String NET = "net";

		default Net getNet() {
			return (Net) getProperty(NET);
		}

		default void setNet(Net net) {
			setProperty(NET, net);
		}
	}

	public interface PropSeed extends Data {
		static final String SEED = "seed";

		default Long getSeed() {
			return (Long) getProperty(SEED);
		}

		default void setSeed(Long seed) {
			setProperty(SEED, seed);
		}
	}

	public interface PropReplicator extends Data {
		static final String REPLICATOR = "replicator";

		default NetReplicator getReplicator() {
			return (NetReplicator) getProperty(REPLICATOR);
		}

		default void setReplicator(NetReplicator replicator) {
			setProperty(REPLICATOR, replicator);
		}
	}

	public interface PropSerialNumber extends Data {
		static final String SERIAL_NUMBER = "serialNumber";

		default Long getSerialNumber() {
			return (Long) getProperty(SERIAL_NUMBER);
		}

		default void setSerialNumber(Long serialNumber) {
			setProperty(SERIAL_NUMBER, serialNumber);
		}
	}

	public interface PropStepsExamResult extends Data {
		static final String STEPS_EXAM_RESULT = "stepsExamResult";

		default StepsExamResult getStepsExamResult() {
			return (StepsExamResult) getProperty(STEPS_EXAM_RESULT);
		}

		default void setStepsExamResult(StepsExamResult stepsExamResult) {
			setProperty(STEPS_EXAM_RESULT, stepsExamResult);
		}
	}

	public interface PropSourceInfo extends Data {
		static final String SOURCE_INFO = "sourceInfo";

		default String getSourceInfo() {
			return (String) getProperty(SOURCE_INFO);
		}

		default void setSourceInfo(String sourceInfo) {
			setProperty(SOURCE_INFO, sourceInfo);
		}
	}

}

package org.ent.dev.unit.local;

import org.ent.dev.unit.combine.ProcProc;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataProxy;
import org.ent.dev.unit.local.util.ITypedProc;
import org.ent.dev.unit.local.util.TypedProcWrapper;

/**
 * Special case of Pipe, where the same data object (possibly modified)
 * is forwarded.
 */
public interface Proc extends Pipe {

	void accept(Data data);

	@Override
	default Data apply(Data data) {
		accept(data);
		return data;
	}

	default Proc combineProc(Proc proc) {
		return new ProcProc(this, proc);
	}

	default <T extends DataProxy> Proc combineProc(Class<T> type, ITypedProc<T> i) {
		return new ProcProc(this, new TypedProcWrapper<>(type, i));
	}

}

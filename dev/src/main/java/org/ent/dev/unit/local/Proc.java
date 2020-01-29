package org.ent.dev.unit.local;

import org.ent.dev.unit.combine.ProcProc;
import org.ent.dev.unit.data.Data;

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

}

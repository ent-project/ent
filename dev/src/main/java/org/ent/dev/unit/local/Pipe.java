package org.ent.dev.unit.local;

import org.ent.dev.unit.combine.PipeFilter;
import org.ent.dev.unit.combine.PipePipe;
import org.ent.dev.unit.data.Data;

public interface Pipe {

	Data apply(Data input);

	default Pipe combinePipe(Pipe p) {
		return new PipePipe(this, p);
	}

	default Filter combineFilter(Filter p) {
		return new PipeFilter(this, p);
	}
}

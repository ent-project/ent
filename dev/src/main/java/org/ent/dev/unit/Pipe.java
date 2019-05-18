package org.ent.dev.unit;

public interface Pipe {

	Data apply(Data input);

	default Pipe combinePipe(Pipe p) {
		return new PipePipe(this, p);
	}

	default Filter combineFilter(Filter p) {
		return new PipeFilter(this, p);
	}
}

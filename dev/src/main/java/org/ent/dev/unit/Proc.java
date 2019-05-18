package org.ent.dev.unit;

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

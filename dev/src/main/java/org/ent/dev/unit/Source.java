package org.ent.dev.unit;

public interface Source {

	Data get();

	default Sup toSup() {
		return new SourceSup(this);
	}

}

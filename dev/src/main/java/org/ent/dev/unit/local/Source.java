package org.ent.dev.unit.local;

import org.ent.dev.unit.Sup;
import org.ent.dev.unit.combine.SourceSup;
import org.ent.dev.unit.data.Data;

public interface Source {

	Data get();

	default Sup toSup() {
		return new SourceSup(this);
	}

}

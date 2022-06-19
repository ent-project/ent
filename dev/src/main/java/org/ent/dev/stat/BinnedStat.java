package org.ent.dev.stat;

public interface BinnedStat {

	int getBinSize();

	int getNoBins();

	double getValue(int bin);

}
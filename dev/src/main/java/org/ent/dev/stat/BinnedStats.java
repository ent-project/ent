package org.ent.dev.stat;

public interface BinnedStats {

	String getTitle();

	int getBinSize();

	int getNoBins();

	double getValue(int bin);

}
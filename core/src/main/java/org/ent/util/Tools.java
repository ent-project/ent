package org.ent.util;

import java.time.Duration;
import java.util.Arrays;

public final class Tools {

	private Tools() {
	}

	/**
	 * Performs a binary search of value x in sorted array a.
	 *
	 * Returns the lowest index i, such that x <= a[i].
	 * If x is greater than the last value of a, the result depends on
	 * parameter 'strict'. If strict is true, throws an exception,
	 * otherwise returns the last array index.
	 */
	public static int binarySearchLowerEqual(double[] a, double x, boolean strict) {
		int idx = Arrays.binarySearch(a, x);
		if (idx < 0) {
			idx = -idx - 1;
		}
		if (idx == a.length) {
			if (strict) {
				throw new IllegalArgumentException(
						"Input value x=" + x + " is greater than the last value (a[" + (a.length - 1)
						+ "]=" + a[a.length - 1] + ")");
			} else {
				idx = a.length - 1;
			}
		}
		if (idx < 0 || idx > a.length) {
			throw new AssertionError("Unexpected index after binary search: " + idx);
		}
		return idx;
	}

	public static String getHitsPerMinute(int numHits, Duration duration) {
		return String.format("%.2f", numHits * 60_000.0 / duration.toMillis());
	}
}

package org.ent.net.io.formatter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.ArithmeticUtils;

public final class VariableNameHelper {

	private static final int RADIX = 26; // number of letters 'a' .. 'z'

	// total number of possible variable names with number of characters
	// less or equal to index i (using letters 'a' .. 'z')
	private static final List<Integer> summedBlockSizes = new ArrayList<>();

	static {
		summedBlockSizes.add(0);
	}

	private VariableNameHelper() {
	}

	/**
	 * Convert zero-based index to variable name:
	 * "a", "b", ..., "z", "aa", "ab", ... , "az", ..., "zz", "aaa", ...
	 */
	public static String getLetterBasedVariableNameForIndex(int index) {
		if (index < 0) {
			throw new IllegalArgumentException();
		}
		int numLetters = getNumberOfLetters(index);
		int offset = index - summedBlockSizes.get(numLetters - 1);
		StringBuilder sb = new StringBuilder();
		int currentValue = offset;
		for (int i = 0; i < numLetters; i++) {
			int ch = currentValue % RADIX;
			sb.append((char) ('a' + ch));
			currentValue = currentValue / RADIX;
		}
		return sb.reverse().toString();
	}

	private static int getNumberOfLetters(int index) {
		int numLetters = 1;
		while (true) {
			if (numLetters >= summedBlockSizes.size()) {
				int limit = summedBlockSizes.get(numLetters - 1) + ArithmeticUtils.pow(RADIX, numLetters);
				summedBlockSizes.add(limit);
			}
			if (summedBlockSizes.get(numLetters) > index) {
				return numLetters;
			}
			numLetters++;
		}
	}

}

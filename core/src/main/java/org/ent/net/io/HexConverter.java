package org.ent.net.io;

public final class HexConverter {

	private HexConverter() {
	}

	public static String toHex(long n) {
		return String.format("%016x", n);
	}

	public static long fromHex(String hex) {
		return Long.parseUnsignedLong(hex, 16);
	}
}

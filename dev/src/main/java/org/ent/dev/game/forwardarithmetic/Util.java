package org.ent.dev.game.forwardarithmetic;

public class Util {
    private Util() {
    }

    public static Integer max(Integer a, Integer b) {
        if (a == null) {
            return b;
        } else {
            if (b == null) {
                return a;
            } else {
                return Math.max(a, b);
            }
        }
    }

    public static Integer min(Integer a, Integer b) {
        if (a == null) {
            return b;
        } else {
            if (b == null) {
                return a;
            } else {
                return Math.min(a, b);
            }
        }
    }
}

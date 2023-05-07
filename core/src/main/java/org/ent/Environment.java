package org.ent;

public class Environment {

    private static boolean isTest;

    private Environment() {
    }

    public static boolean isTest() {
        return isTest;
    }

    public static void setTest(boolean isTest) {
        Environment.isTest = isTest;
    }
}

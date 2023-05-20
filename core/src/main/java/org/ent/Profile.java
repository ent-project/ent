package org.ent;

public class Profile {

    private static boolean isTest;

    private Profile() {
    }

    public static boolean isTest() {
        return isTest;
    }

    public static void setTest(boolean isTest) {
        Profile.isTest = isTest;
    }

    public static void verifyTestProfile() {
        if (!isTest()) {
            throw new AssertionError();
        }
    }
}

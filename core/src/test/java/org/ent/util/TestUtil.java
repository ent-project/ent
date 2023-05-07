package org.ent.util;

import org.apache.commons.lang3.StringUtils;

public class TestUtil {
    private TestUtil() {
    }

    public static String toBinary16bit(int i) {
        return StringUtils.leftPad(Integer.toBinaryString(i), 16, '0');
    }

}

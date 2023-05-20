package org.ent;

import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class TestBase {

    protected NetParser parser;

    protected NetFormatter formatter;

    @BeforeEach
    void setUpParserAndFormatter() {
        parser = new NetParser();
        formatter = new NetFormatter()
                .withAscii(true)
                .withForceGivenNodeNames(true);
    }

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }
}

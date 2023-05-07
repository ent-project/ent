package org.ent.net.io.parser.tokenizer;

public class ValueToken extends Token {

    private final int value;

    public ValueToken(String hexDigits) {
        super("{@value=#" + hexDigits + "}");
        this.value = Integer.parseInt(hexDigits, 16);
    }

    public int getValue() {
        return value;
    }
}

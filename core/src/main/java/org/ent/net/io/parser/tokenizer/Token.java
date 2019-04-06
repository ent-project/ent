package org.ent.net.io.parser.tokenizer;

public class Token {

    private final String text;

    public Token(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

package org.ent.net.io.parser.tokenizer;

public class Token {

    String text;

    public Token(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

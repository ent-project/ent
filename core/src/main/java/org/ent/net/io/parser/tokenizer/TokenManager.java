package org.ent.net.io.parser.tokenizer;

public final class TokenManager {

    public static final Token TOKEN_LEFT_PARENTHESIS = new Token("(");
    public static final Token TOKEN_RIGHT_PARENTHESIS = new Token(")");
    public static final Token TOKEN_LEFT_SQUARE_BRACKET = new Token("[");
    public static final Token TOKEN_RIGHT_SQUARE_BRACKET = new Token("]");
    public static final Token TOKEN_COMMA = new Token(",");
    public static final Token TOKEN_SEMICOLON = new Token(";");
    public static final Token TOKEN_EQUALS = new Token("=");
    public static final Token TOKEN_MARKER = new Token("#");
    public static final Token TOKEN_EOF = new Token("<EOF>");

    private TokenManager() {
    }
}

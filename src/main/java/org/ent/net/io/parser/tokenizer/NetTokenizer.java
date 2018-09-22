package org.ent.net.io.parser.tokenizer;

import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_COMMA;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_EOF;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_EQUALS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_LEFT_PARENTHESIS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_LEFT_SQUARE_BRACKET;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_RIGHT_PARENTHESIS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_RIGHT_SQUARE_BRACKET;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_SEMICOLON;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.ent.Main;
import org.ent.net.io.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetTokenizer {

	private final Logger log = LoggerFactory.getLogger(Main.class);

    private final Reader reader;
    private int ch;
    private final Deque<Token> queue = new ArrayDeque<>();
    private String value;
    private int line;
    private int column;

    public NetTokenizer(Reader reader) {
        this.reader = reader;
        this.line = 1;
        this.column = 1;
        nextChar();
    }

    private void nextChar() {
        try {
            this.ch = this.reader.read();
            if (this.ch == '\n') {
                this.line++;
                this.column = 1;
            } else {
                this.column++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Token next() throws ParserException {
        if (!this.queue.isEmpty()) {
            return this.queue.remove();
        }
        Token next = fetchNextToken();
        log.trace("TOKEN: {}", next);
        return next;
    }

    private Token fetchNextToken() throws ParserException {
        while (true) {
            boolean advanced = false;
            if (Character.isWhitespace(this.ch)) {
                nextChar();
                advanced = true;
            }
            if (this.ch == '#') {
                // comment - advance to the next newline or end of file
                nextChar();
                while (this.ch != '\n' && this.ch != -1) {
                    nextChar();
                }
                advanced = true;
            }
            if (!advanced) {
                break;
            }
        }
        switch (this.ch) {
            case -1:
                nextChar();
                return TOKEN_EOF;
            case '(':
                nextChar();
                return TOKEN_LEFT_PARENTHESIS;
            case ')':
                nextChar();
                return TOKEN_RIGHT_PARENTHESIS;
            case '[':
                nextChar();
                return TOKEN_LEFT_SQUARE_BRACKET;
            case ']':
                nextChar();
                return TOKEN_RIGHT_SQUARE_BRACKET;
            case '=':
                nextChar();
                return TOKEN_EQUALS;
            case ',':
                nextChar();
                return TOKEN_COMMA;
            case ';':
                nextChar();
                return TOKEN_SEMICOLON;
            case '<':
                nextChar();
                return parseCommand();
            default:
                if (isIdentifierStartChar(this.ch)) {
                    return parseIdentifier();
                } else {
                    throw new ParserException(
                            "Unexpected character '" + (char) this.ch + "' in line " + this.line + " column " + this.column);
                }
        }
    }

    public Token peek() throws ParserException {
        return peek(1);
    }

    public Token peek(int n) throws ParserException {
        while (queue.size() < n) {
            Token tkn = fetchNextToken();
            log.trace("TOKEN: {}", tkn);
            this.queue.add(tkn);
        }
        return (new ArrayList<>(queue)).get(n - 1);
    }

    public String getValue() {
        return this.value;
    }

    private static boolean isLetter(int ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private static boolean isNumber(int ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isIdentifierStartChar(int ch) {
        return isLetter(ch) || ch == '_';
    }

    private static boolean isIdentifierChar(int ch) {
        return isIdentifierStartChar(ch) || isNumber(ch);
    }

    private Token parseCommand() {
        StringBuilder sb = new StringBuilder();
        while (this.ch != -1 && this.ch != '>') {
            sb.append((char) this.ch);
            nextChar();
        }
        if (this.ch == '>') {
            nextChar();
        }
        return new CommandToken(sb.toString());
    }

    private Token parseIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (isIdentifierChar(ch)) {
            sb.append((char) this.ch);
            nextChar();
        }
        return new IdentifierToken(sb.toString());
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}

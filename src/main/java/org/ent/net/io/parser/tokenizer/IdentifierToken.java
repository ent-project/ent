package org.ent.net.io.parser.tokenizer;

public class IdentifierToken extends Token {

    private final String name;

	public IdentifierToken(String name) {
        super("{@id=" + name + "}");
        this.name = name;
    }

    public String getName() {
		return name;
	}
}
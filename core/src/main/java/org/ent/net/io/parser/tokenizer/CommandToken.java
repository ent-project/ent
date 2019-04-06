package org.ent.net.io.parser.tokenizer;

public class CommandToken extends Token {

    private final String commandName;

	public CommandToken(String commandName) {
        super("{@cmd=<" + commandName + ">}");
        this.commandName = commandName;
    }

    public String getCommandName() {
		return commandName;
	}

}
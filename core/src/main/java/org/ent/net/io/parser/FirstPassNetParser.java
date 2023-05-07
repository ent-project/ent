package org.ent.net.io.parser;

import org.ent.net.io.parser.tokenizer.CommandToken;
import org.ent.net.io.parser.tokenizer.IdentifierToken;
import org.ent.net.io.parser.tokenizer.NetTokenizer;
import org.ent.net.io.parser.tokenizer.Token;
import org.ent.net.io.parser.tokenizer.TokenManager;
import org.ent.net.io.parser.tokenizer.ValueToken;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;

import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_COLON;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_COMMA;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_EOF;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_LEFT_PARENTHESIS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_LEFT_SQUARE_BRACKET;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_RIGHT_PARENTHESIS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_RIGHT_SQUARE_BRACKET;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_SEMICOLON;

public class FirstPassNetParser {

	private final NetTokenizer tokenizer;

    private final Map<String, NodeTemplate> identifierMapping = new HashMap<>();

	private final Set<NodeTemplate> allEntries = new LinkedHashSet<>();

	private boolean markerNodePermitted;

    public FirstPassNetParser(Reader reader) {
    	this.tokenizer = new NetTokenizer(reader);
	}

    public void setMarkerNodesPermitted() {
    	this.markerNodePermitted = true;
    }

	public Set<NodeTemplate> getAllEntries() {
		return allEntries;
	}

    public Map<String, NodeTemplate> getIdentifierMapping() {
		return identifierMapping;
	}

	public List<NodeTemplate> parseAll() throws ParserException {
        List<NodeTemplate> result = new ArrayList<>();
        while (tokenizer.peek() == TOKEN_SEMICOLON) {
            tokenizer.next();
        }
        while (true) {
            result.add(parseNodeExpression());
            if (tokenizer.peek() == TOKEN_EOF) {
                break;
            }
            demandToken(TOKEN_SEMICOLON);
            while (tokenizer.peek() == TOKEN_SEMICOLON) {
                tokenizer.next();
            }
            if (tokenizer.peek() == TOKEN_EOF) {
                break;
            }
        }
        return result;
    }

    public NodeTemplate resolveIdentifier(IdentifierNodeTemplate template) throws ParserException {
    	return doResolveIdentifier(template, 0);
    }

    private NodeTemplate doResolveIdentifier(IdentifierNodeTemplate template, int level) throws ParserException {
    	if (level > identifierMapping.size()) {
    		throw new ParserException("Cyclic identifier binding");
    	}
    	NodeTemplate mappedTemplate = identifierMapping.get(template.getName());
        if (mappedTemplate == null) {
            throw new ParserException("Unknown identifier: '" + template.getName() + "'");
        }

        if (mappedTemplate instanceof IdentifierNodeTemplate identifierNodeTemplate) {
        	return doResolveIdentifier(identifierNodeTemplate, level + 1);
        } else {
        	return mappedTemplate;
        }
    }

    private NodeTemplate parseNodeExpression() throws ParserException {
        Token token = tokenizer.peek();
        if (token instanceof IdentifierToken identifierToken) {
            Token tokenNext = tokenizer.peek(2);
            if (tokenNext == TOKEN_COLON) {
                tokenizer.next();
                tokenizer.next();
                NodeTemplate nodeTemplate = parseNodeWithPossibleValueExpression();
                String name = identifierToken.getName();
                if (identifierMapping.containsKey(name))
                    throw new ParserException("Identifier '" + name + "' bound more than once.");
                identifierMapping.put(name, nodeTemplate);
                allEntries.add(nodeTemplate);
                return nodeTemplate;
            }
        }
        NodeTemplate nodeTemplate = parseNodeWithPossibleValueExpression();
        allEntries.add(nodeTemplate);
        return nodeTemplate;
    }

    private NodeTemplate parseNodeWithPossibleValueExpression() throws ParserException {
        Token token = tokenizer.peek();
        int value = 0;
        if (token instanceof ValueToken valueToken) {
            tokenizer.next();
            value = valueToken.getValue();
        } else if (token instanceof CommandToken commandToken) {
            tokenizer.next();
            Command command = CommandFactory.getByName(commandToken.getCommandName());
            if (command == null) {
                throw new ParserException("Unknown command: '" + commandToken.getCommandName() + "'");
            }
            value = command.getValue();
        } else {
            return parsePlainNodeExpression(0);
        }
        Token next = tokenizer.peek();
        if (next == TOKEN_LEFT_SQUARE_BRACKET || next == TOKEN_LEFT_PARENTHESIS) {
            return parsePlainNodeExpression(value);
        } else {
            return new ValueNodeTemplate(value);
        }
    }

        private NodeTemplate parsePlainNodeExpression(int value) throws ParserException {
        Token token = tokenizer.next();
        if (token == TOKEN_LEFT_SQUARE_BRACKET) {
            NodeTemplate child = parseNodeExpression();
            demandToken(TOKEN_RIGHT_SQUARE_BRACKET);
            return new UnaryNodeTemplate(value, child);
        } else if (token == TOKEN_LEFT_PARENTHESIS) {
            NodeTemplate leftChild = parseNodeExpression();
            demandToken(TOKEN_COMMA);
            NodeTemplate rightChild = parseNodeExpression();
            demandToken(TOKEN_RIGHT_PARENTHESIS);
            return new BNodeTemplate(value, leftChild, rightChild);
        } else if (token instanceof IdentifierToken identifierToken) {
            return new IdentifierNodeTemplate(identifierToken.getName());
        } else if (token == TokenManager.TOKEN_MARKER) {
        	if (markerNodePermitted) {
            	return new MarkerNodeTemplate();
        	} else {
        		throw new ParserException(MessageFormat.format(
        				"Found marker node in line {0}, column {1}, but is not permitted",
        				tokenizer.getLine(), tokenizer.getColumn()));
        	}
        } else {
            throw new ParserException(MessageFormat.format(
                    "Unexpected token ''{0}'' in line {1}, column {2}",
                    token, tokenizer.getLine(), tokenizer.getColumn()));
        }
    }

    private void demandToken(Token expectedToken) throws ParserException {
        Token nextToken = tokenizer.next();
        if (nextToken != expectedToken) {
            throw new ParserException(MessageFormat.format(
                    "Expected token ''{0}'', but got ''{1}'' in line {2}, column {3}",
                    expectedToken, nextToken, tokenizer.getLine(), tokenizer.getColumn()));
        }
    }

}

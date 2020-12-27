package org.ent.net.io.parser;

import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_COMMA;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_EOF;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_EQUALS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_LEFT_PARENTHESIS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_LEFT_SQUARE_BRACKET;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_RIGHT_PARENTHESIS;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_RIGHT_SQUARE_BRACKET;
import static org.ent.net.io.parser.tokenizer.TokenManager.TOKEN_SEMICOLON;

import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ent.net.io.parser.tokenizer.CommandToken;
import org.ent.net.io.parser.tokenizer.IdentifierToken;
import org.ent.net.io.parser.tokenizer.NetTokenizer;
import org.ent.net.io.parser.tokenizer.Token;
import org.ent.net.io.parser.tokenizer.TokenManager;
import org.ent.net.node.MarkerNode;

public class FirstPassNetParser {

	private final NetTokenizer tokenizer;

    private final Map<String, NodeTemplate> identifierMapping = new HashMap<>();

	private final Set<NodeTemplate> allEntries = new HashSet<>();

	private boolean markerNodePermitted;

	private MarkerNode markerNode;

    public FirstPassNetParser(Reader reader) {
    	this.tokenizer = new NetTokenizer(reader);
	}

    public void setMarkerNodesPermitted(MarkerNode markerNode) {
    	this.markerNodePermitted = true;
    	this.markerNode = markerNode;
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
            if (tokenNext == TOKEN_EQUALS) {
                tokenizer.next();
                tokenizer.next();
                NodeTemplate value = parsePlainNodeExpression();
                String name = identifierToken.getName();
                if (identifierMapping.containsKey(name))
                    throw new ParserException("Identifier '" + name + "' bound more than once.");
                identifierMapping.put(name, value);
                allEntries.add(value);
                return value;
            }
        }
        NodeTemplate value = parsePlainNodeExpression();
        allEntries.add(value);
        return value;
    }

    private NodeTemplate parsePlainNodeExpression() throws ParserException {
        Token token = tokenizer.next();
        if (token instanceof CommandToken) {
            return new CommandNodeTemplate(((CommandToken) token).getCommandName());
        } else if (token == TOKEN_LEFT_SQUARE_BRACKET) {
            NodeTemplate child = parseNodeExpression();
            demandToken(TOKEN_RIGHT_SQUARE_BRACKET);
            return new UNodeTemplate(child);
        } else if (token == TOKEN_LEFT_PARENTHESIS) {
            NodeTemplate leftChild = parseNodeExpression();
            demandToken(TOKEN_COMMA);
            NodeTemplate rightChild = parseNodeExpression();
            demandToken(TOKEN_RIGHT_PARENTHESIS);
            return new BNodeTemplate(leftChild, rightChild);
        } else if (token instanceof IdentifierToken) {
            return new IdentifierNodeTemplate(((IdentifierToken) token).getName());
        } else if (token == TokenManager.TOKEN_MARKER) {
        	if (markerNodePermitted) {
            	return new MarkerNodeTemplate(markerNode);
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

package org.ent.net.node.cmd.operation;

import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;

public class EvalOperation implements BiOperation<Node, Node> {

	private static final char[] SUPERSCRIPT_NUMBERS = new char[] { '⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸', '⁹' };

	private final int evalLevel;

	private final String shortName;

	private final String shortNameAscii;

	public EvalOperation(int evalLevel) {
		this.evalLevel = evalLevel;
		this.shortName = "🞜" + toSuperscriptNumber(evalLevel);
		this.shortNameAscii = "eval" + evalLevel;
	}

	@Override
	public ExecutionResult apply(Node node1, Node node2) {
		if (!(node1 instanceof CNode cNode)) {
			return ExecutionResult.ERROR;
		}
		Command command = cNode.getCommand();
		if (command.getEvalLevel() >= evalLevel) {
			return ExecutionResult.ERROR;
		}

		command.execute(node2);
		return ExecutionResult.NORMAL;
	}

	@Override
	public int getEvalLevel() {
		return evalLevel;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getShortNameAscii() {
		return shortNameAscii;
	}

	static String toSuperscriptNumber(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("input must be greater or equal 0!");
		}
		String s = Integer.toString(n);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ('0' <= c && c <= '9') {
				sb.append(SUPERSCRIPT_NUMBERS[c - '0']);
			} else {
				throw new AssertionError();
			}
		}
		return sb.toString();
	}
}

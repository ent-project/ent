package org.ent.net;

public class NetWithStringRepresentation {

	private final Net net;

    private final String stringRepresentation;

    public NetWithStringRepresentation(Net net, String serialized) {
		super();
		this.net = net;
		this.stringRepresentation = serialized;
	}

	public Net getNet() {
		return net;
	}

    public String getStringRepresentation() {
		return stringRepresentation;
	}

}
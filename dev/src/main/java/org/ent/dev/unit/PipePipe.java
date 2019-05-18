package org.ent.dev.unit;

public class PipePipe implements Pipe {

	private Pipe delegateUp;

	private Pipe delegateDown;

	public PipePipe(Pipe delegateUp, Pipe delegateDown) {
		this.delegateUp = delegateUp;
		this.delegateDown = delegateDown;
	}

	@Override
	public Data apply(Data data) {
		Data intermediate = delegateUp.apply(data);
		return delegateDown.apply(intermediate);
	}

}

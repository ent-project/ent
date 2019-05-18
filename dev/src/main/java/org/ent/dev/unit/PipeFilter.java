package org.ent.dev.unit;

public class PipeFilter implements Filter {

	private Pipe pipeDelegate;

	private Filter filterDelegate;

	public PipeFilter(Pipe pipeDelegate, Filter filterDelegate) {
		this.pipeDelegate = pipeDelegate;
		this.filterDelegate = filterDelegate;
	}

	@Override
	public boolean test(Data element) {
		Data intermediate = pipeDelegate.apply(element);
		return filterDelegate.test(intermediate);
	}

}

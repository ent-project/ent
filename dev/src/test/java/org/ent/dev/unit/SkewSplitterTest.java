package org.ent.dev.unit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SkewSplitterTest {

	private Data dataUpstream = new DataImpl();
	private Data dataHeavyLane = new DataImpl();
	private Data dataLightLane = new DataImpl();

	private SkewSplitter skewSplitter = new SkewSplitter();

	@Spy
	private Sup upstream = new SourceSup(() -> dataUpstream);

	@Spy
	private Dan heavyLane = new PipeDan(data -> dataHeavyLane);

	@Mock
	private Pipe lightLane;

	@Spy
	private SinkReq downstream = new SinkReq(data -> {});

	@BeforeEach
	public void setUp() {
		skewSplitter.setHeavyLane(heavyLane);
		skewSplitter.setLightLane(lightLane);

		wire(upstream, skewSplitter);
		wire(skewSplitter, downstream);
	}

	@Test
	public void integration_pass_pipedThroughLightLane() {
		when(lightLane.apply(dataUpstream)).thenReturn(dataLightLane);
		Filter sorter = data -> true;
		skewSplitter.setSorter(sorter);

		downstream.poll();
		while (DeliveryStash.instance.hasWork()) {
			DeliveryStash.instance.work();
		}

		verifySetup();

		verify(upstream).requestNext();
		verifyNoMoreInteractions(upstream);

		verify(heavyLane).requestNext();
		verifyNoMoreInteractions(heavyLane);

		verify(lightLane).apply(dataUpstream);
		verifyNoMoreInteractions(lightLane);

		verify(downstream).deliver(dataLightLane);
		verify(downstream).receiveNext(dataLightLane);
		verifyNoMoreInteractions(downstream);
	}

	@Test
	public void integration_noPass_deliveredToHeavyLane() {
		Filter sorter = data -> false;
		skewSplitter.setSorter(sorter);

		downstream.poll();
		while (DeliveryStash.instance.hasWork()) {
			DeliveryStash.instance.work();
		}

		verifySetup();

		verify(upstream).requestNext();
		verifyNoMoreInteractions(upstream);

		verify(heavyLane).requestNext();
		verify(heavyLane).deliver(dataUpstream);
		verify(heavyLane).receiveNext(dataUpstream);
		verifyNoMoreInteractions(heavyLane);

		verifyNoMoreInteractions(lightLane);

		verify(downstream).deliver(dataHeavyLane);
		verify(downstream).receiveNext(dataHeavyLane);
		verifyNoMoreInteractions(downstream);
	}

	private void wire(Sup sup, Req req) {
		sup.setDownstream(req);
		req.setUpstream(sup);
	}

	private void verifySetup() {
		verify(upstream).setDownstream(skewSplitter);

		verify(heavyLane).setUpstream(upstream);
		verify(heavyLane).setDownstream(downstream);

		verify(downstream).setUpstream(skewSplitter);
		verify(downstream).poll();
	}

}

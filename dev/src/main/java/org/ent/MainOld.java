package org.ent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueCandidate;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainOld {

	private final Logger log = LoggerFactory.getLogger(MainOld.class);

	public static void main(String[] args) throws Exception {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		StatusPrinter.print(lc);

		new MainOld().run3();
	}

	private int steps;
	private List<ValueCandidate> valueCandidates;

	public void run3() throws Exception {
		NetParser parser = new NetParser();
		Net net = parser.parse("A=((_a=<↓⏪•>, B=([B], C=(_a, C))), ((<⤩>, (A, _b=<🞜⁵>)), _b))");
		NetFormatter formatter = new NetFormatter();
		String r  = formatter.format(net);
		System.err.println(r);
		dumpExec(net);
	}

	private void dumpExec(Net net) {
		ch.qos.logback.classic.Logger execLogger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(RunSetup.class);
		Level prevLevel = execLogger.getLevel();
		execLogger.setLevel(Level.TRACE);
		ManagedRun execManager = getExecutionManager(net);
		execManager.perform();
		execLogger.setLevel(prevLevel);
	}


	public void run() {
		valueCandidates = setupCommandCandidates();
		next(3681681368538974412L);
	}

	public void run1() {
		log.error("sdf");

		Random randSeeds = new Random(0x12345abcL);

		valueCandidates = setupCommandCandidates();


//		List<Command> ls = new ArrayList<>();
//		for (int i = 1; i<=100; i++) {
//			Command cmd = drawing.drawCommand();
//			ls.add(cmd);
//		}
//		System.err.println(ls.stream().map(c->c.getShortName()).collect(Collectors.toList()));

		for (int i = 1; i<=5000000; i++) {
			try {
				long netSeed = randSeeds.nextLong();
				Net net = drawNet(netSeed);
//				NetFormatter formatter = new NetFormatter();
//				System.err.println(formatter.format(net));

				ManagedRun ee = getExecutionManager(net);
				ee.perform();
				steps = ee.getNoSteps();
//				System.err.println("steps: " + steps);
				if (steps > 1 && steps < 20) {
//					System.err.println("seed: " + netSeed);
					next(netSeed);
//					return;
				}

			} catch (RuntimeException re) {
//				System.err.println("rejected!");
			}
		}

	}

	private ManagedRun getExecutionManager(Net net) {
		RunSetup setup = getRunSetup();
		ManagedRun mr = new ManagedRun(setup);
		Ent ent = new Ent(net); // FIXME
		mr.setEnt(ent);
		return mr;
	}

	private RunSetup getRunSetup() {
		return null;
//		return new RunSetup.Builder()
//				.withCommandExecutionFailedIsFatal(true)
//				.withInvalidCommandBranchIsFatal(true)
//				.withInvalidCommandNodeIsFatal(true)
//				.withMaxSteps(20) // TODO: Hyperparameter
//				.build();
	}

	private Net drawNet(long netSeed) {
		Random rand = new Random(netSeed);
		ValueDrawing drawing = new DefaultValueDrawing();
		RandomNetCreator creator = new RandomNetCreator(15, rand, drawing);
		return creator.drawNetMaybe().orElseThrow(() -> new RuntimeException());
	}

	private void next(long netSeed) {
//		Random rand = new Random(netSeed);
//		CommandDrawing drawing = new CommandDrawingImpl(rand, commandCandidates);
//		RandomNetCreator creator = new RandomNetCreator(rand, drawing);
//		Optional<Net> maybeNet = creator.drawNet();
//		if (!maybeNet.isPresent()) {
//			return;
//		}
//		Net net = maybeNet.get();
//		NetFormatter formatter = new NetFormatter();
//
//		NetTrimmer trimmer = new NetTrimmer(net, getRunSetup());
//		trimmer.runTrimmer();
//
//		System.err.println(steps + "  " + formatter.format(net));
//
//		ManagedRun ee = getExecutionManager(net);
//		ee.perform();

	}

	private List<ValueCandidate> setupCommandCandidates() {
		List<ValueCandidate> valueCandidates = new ArrayList<>();

		int N1 = 9;
		int N3 = 3;
		int N9 = 1;
		int MAX_EVAL_LEVEL = 5;
		double N_EVAL = N1 / MAX_EVAL_LEVEL;

		valueCandidates.add(new ValueCandidate(Commands.NOP, N1));
		for (ArrowDirection left : ArrowDirection.values()) {
			valueCandidates.add(new ValueCandidate(Commands.get(Operations.SET_OPERATION, Accessors.get(left), Accessors.DIRECT), N3));
			for (ArrowDirection right : ArrowDirection.values()) {
				valueCandidates.add(new ValueCandidate(Commands.get(Operations.SET_OPERATION, Accessors.get(left), Accessors.get(right)), N9));
			}
		}
		for (ArrowDirection left : ArrowDirection.values()) {
			valueCandidates.add(new ValueCandidate(Commands.get(Operations.DUP_OPERATION, Accessors.get(left), Accessors.DIRECT), N3));
		}
		valueCandidates.add(new ValueCandidate(Commands.ANCESTOR_EXCHANGE, N1));
//		for (int evalLevel = 1; evalLevel <= MAX_EVAL_LEVEL; evalLevel++) {
//			commandCandidates.add(new CommandCandidate(CommandFactory.createEvalCommand(evalLevel), N_EVAL));
//		}
//		for (ArrowDirection left : ArrowDirection.values()) {
//			for (ArrowDirection right : ArrowDirection.values()) {
//				valueCandidates.add(new ValueCandidate(Commands.get(Operations.IS_IDENTICAL_OPERATION, Accessors.get(left), Accessors.get(right)), N9));
//			}
//		}
		return valueCandidates;
	}

}


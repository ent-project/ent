package org.ent.dev.trim;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.rng.UniformRandomProvider;
import org.ent.Ent;
import org.ent.listener.NopEntEventListener;
import org.ent.Profile;
import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.util.RandomUtil;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrimmingHelperTest {
    public static final int MAX_STEPS = 200;
    public static final int NUMBER_OF_NODES = 80;
    public static final long RANDOM_SEED = 1234L;
    private static final int MURMUR_SEED = 0x234f3a;
    private static final HashFunction MURMUR_3 = Hashing.murmur3_32_fixed(MURMUR_SEED);
    private static final DefaultValueDrawing DRAWING = new DefaultValueDrawing();

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    @Test
    void sameCommandsExecuted() {
        UniformRandomProvider randMaster = RandomUtil.newRandom(RANDOM_SEED);
        for (int i = 0; i < 1000; i++) {
            long netSeed = randMaster.nextLong();
            RandomNetCreator netCreator0 = new RandomNetCreator(NUMBER_OF_NODES, RandomUtil.newRandomNoScramble(netSeed), DRAWING);
            Net net0 = netCreator0.drawNet();
            TrimmingListener trimmingListener = new TrimmingListener(net0.getNodes().size());
            net0.addEventListener(trimmingListener);
            int hash0 = executeAndGetHash(net0);

            RandomNetCreator netCreator1 = new RandomNetCreator(NUMBER_OF_NODES, RandomUtil.newRandomNoScramble(netSeed), DRAWING);
            Net net1 = netCreator1.drawNet();
            TrimmingHelper.trim(net1, trimmingListener);
            int hash1 = executeAndGetHash(net1);

            assertThat(hash1).isEqualTo(hash0);
        }
    }

    private static int executeAndGetHash(Net net) {
        Ent ent = new Ent(net);

        class HashingEntEventListener extends NopEntEventListener {
            Hasher hasher = MURMUR_3.newHasher();

            @Override
            public void beforeCommandExecution(Node executionPointer, Command command) {
                hasher.putInt(executionPointer.getIndex());
                hasher.putInt(executionPointer.getValue());
            }

            @Override
            public void afterCommandExecution(StepResult stepResult) {
                hasher.putInt(stepResult.ordinal());
            }
        }
        HashingEntEventListener entEventListener = new HashingEntEventListener();
        ent.addEventListener(entEventListener);

        EntRunner runner = new EntRunner(ent);
        for (int i = 0; i < MAX_STEPS; i++) {
            runner.step();
        }
        return entEventListener.hasher.hash().asInt();
    }
}
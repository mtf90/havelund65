package com.github.mtf90.havelund65.benchmark;

import java.util.Map;
import java.util.Random;

import com.github.mtf90.havelund65.api.SPAAlphabet;
import com.github.mtf90.havelund65.api.Example;
import com.github.mtf90.havelund65.impl.SOSMonitor;
import com.github.mtf90.havelund65.impl.SOSMonitor.Stats;
import com.github.mtf90.havelund65.impl.SULGenerator;
import com.github.mtf90.havelund65.impl.SULGenerator.GenerationException;
import net.automatalib.automata.fsa.DFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner<I> {

    private static Logger LOGGER = LoggerFactory.getLogger(Runner.class);
    private static int RETRIES = 1000;

    private final Example<I> example;
    private final long logFrom;
    private final long length;
    private final double procWeight;

    private final Random random;

    static {
        LOGGER.info(
                "procWeight, totalSymbols, maximumResourceConsumption, reproductionLength, localCELength, stackHeight, numReturns");
    }

    public Runner(Example<I> example, long logFrom, long length, double procWeight, int seed) {
        this.example = example;
        this.logFrom = logFrom;
        this.length = length;
        this.procWeight = procWeight;
        this.random = new Random(seed);
    }

    public void run() {
        final SPAAlphabet<I> alphabet = example.alphabet();
        final Map<I, DFA<Object, I>> spa = (Map<I, DFA<Object, I>>) example.automaton();
        final I init = example.initialProcedure();

        int exceptions = 0;
        long logCounter = this.logFrom;

        outer:
        for (int retry = 0; retry < RETRIES; retry++) {

            final SULGenerator<?, I> generator = new SULGenerator<>(alphabet, spa, init, procWeight, random);
            final SOSMonitor<?, I> monitor = new SOSMonitor<>(alphabet, spa, init);

            long curr = System.currentTimeMillis();

            for (long i = 1; i <= length; i++) {
                try {
                    final I symbol = generator.generate();
                    // System.err.print(symbol);
                    final boolean successful = monitor.monitor(symbol);

                    assert generator.currState().equals(monitor.currState()) : "States don't match";
                    assert generator.currProc().equals(monitor.currProc()) : "Procedures don't match";
                    assert successful : "generator should only generate valid traces";
                } catch (GenerationException ge) {
                    exceptions++;
                    LOGGER.debug("Caught", ge);
                    continue outer;
                }

                if (i == logCounter) {
                    logStats(monitor.reportStatistics());
                    logCounter *= 10;
                }

//                if (i % 1000000 == 0) {
//                    final long newCurr = System.currentTimeMillis();
//                    LOGGER.debug("{}: {}s", i, (newCurr - curr) / 1000f);
//                    curr = newCurr;
//                }
            }

            LOGGER.debug("Caught {} exceptions", exceptions);
            return;
        }

        throw new IllegalStateException("Could not generate trace after " + RETRIES + " attempts");
    }

    private void logStats(Stats stats) {
        LOGGER.info("{}, {}, {}, {}, {}, {}, {}",
                    this.procWeight,
                    stats.globalSymbolCount,
                    stats.maxResourceConsumption,
                    stats.reproductionLength,
                    stats.localCELength,
                    stats.stackHeight,
                    stats.numReturns);
    }
}

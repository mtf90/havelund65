package com.github.mtf90.havelund65;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.mtf90.havelund65.impl.FastSPAAlphabet;
import com.github.mtf90.havelund65.api.SPAAlphabet;
import com.github.mtf90.havelund65.impl.SOSMonitor;
import com.github.mtf90.havelund65.impl.SOSMonitor.Stats;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MonitorTest {

    private final SPAAlphabet<Character> alphabet;
    private final Map<Character, CompactDFA<Character>> procedures;

    public MonitorTest() {
        final Alphabet<Character> callAlphabet = Alphabets.singleton('S');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');

        alphabet = new FastSPAAlphabet<>(internalAlphabet, callAlphabet, 'R');

        final Set<Character> joinedSymbols = new HashSet<>();
        joinedSymbols.addAll(alphabet.getCallAlphabet());
        joinedSymbols.addAll(alphabet.getInternalAlphabet());

        final Alphabet<Character> joinedAlphabet = Alphabets.fromCollection(joinedSymbols);

        // @formatter:off
        final CompactDFA<Character> proc = AutomatonBuilders.newDFA(joinedAlphabet)
                                                                  .withInitial("s0")
                                                                  .from("s0")
                                                                        .on('a').to("s1")
                                                                        .on('c').to("s2")
                                                                  .from("s1")
                                                                        .on('S').loop()
                                                                        .on('b').to("s2")
                                                                  .withAccepting("s2")
                                                                  .create();
        // @formatter:on

        this.procedures = Collections.singletonMap('S', DFAs.complete(proc, joinedAlphabet));
    }

    @Test
    public void testStatisticsFlat() {

        final SOSMonitor<?, Character> m = new SOSMonitor<>(alphabet, procedures, 'S');
        final Word<Character> accepted = Word.fromCharSequence("SaSaScRScRScR");

        accepted.forEach(i -> Assert.assertTrue(m.monitor(i)));
        validateStats(m.reportStatistics(), new long[] {7, 13, 4, 8, 3, 2});

        Assert.assertTrue(m.monitor('b'));
        validateStats(m.reportStatistics(), new long[] {8, 14, 5, 8, 3, 2});

        Assert.assertTrue(m.monitor('R'));
        validateStats(m.reportStatistics(), new long[] {3, 15, 2, 8, 4, 1});
    }

    @Test
    public void testStatisticsNested() {

        final SOSMonitor<?, Character> m = new SOSMonitor<>(alphabet, procedures, 'S');
        final Word<Character> w1 = Word.fromCharSequence("SaSaSaSaSa");

        w1.forEach(i -> Assert.assertTrue(m.monitor(i)));
        validateStats(m.reportStatistics(), new long[] {10, 10, 1, 10, 0, 5});

        final Word<Character> w2 = Word.fromCharSequence("bRbRbRSaSaSa");
        w2.forEach(i -> Assert.assertTrue(m.monitor(i)));
        validateStats(m.reportStatistics(), new long[] {11, 22, 1, 11, 3, 5});

        final Word<Character> w3 = Word.fromCharSequence("bRbRbRbR");
        w3.forEach(i -> Assert.assertTrue(m.monitor(i)));
        validateStats(m.reportStatistics(), new long[] {3, 30, 2, 12, 7, 1});
    }

    private static void validateStats(Stats actual, long[] expected) {
        Assert.assertEquals(actual.reproductionLength, expected[0]);
        Assert.assertEquals(actual.globalSymbolCount, expected[1]);
        Assert.assertEquals(actual.localCELength, expected[2]);
        Assert.assertEquals(actual.maxResourceConsumption, expected[3]);
        Assert.assertEquals(actual.numReturns, expected[4]);
        Assert.assertEquals(actual.stackHeight, expected[5]);
    }
}

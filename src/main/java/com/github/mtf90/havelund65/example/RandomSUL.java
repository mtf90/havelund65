package com.github.mtf90.havelund65.example;

import java.util.Map;
import java.util.Random;

import com.github.mtf90.havelund65.impl.FastSPAAlphabet;
import com.google.common.collect.Maps;
import com.github.mtf90.havelund65.api.Example;
import com.github.mtf90.havelund65.api.SPAAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class RandomSUL implements Example<Integer> {

    private final SPAAlphabet<Integer> alphabet;
    private final Map<Integer, CompactDFA<Integer>> automaton;

    public RandomSUL(int procedureSize, int seed) {

        final Alphabet<Integer> callAlphabet = Alphabets.integers(0, 9);
        final Alphabet<Integer> internalAlphabet = Alphabets.integers(10, 34);
        final Integer returnSymbol = 50;

        this.alphabet = new FastSPAAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);

        final int combinedAlphabetSize = callAlphabet.size() + internalAlphabet.size();

        this.automaton = Maps.newHashMapWithExpectedSize(combinedAlphabetSize);
        final Alphabet<Integer> combinedAlphabet = Alphabets.integers(0,34);

        final Random random = new Random(seed);

        for (final Integer procedure : callAlphabet) {
            final CompactDFA<Integer> dfa = RandomAutomata.randomICDFA(random, procedureSize, combinedAlphabet, true);
            this.automaton.put(procedure, dfa);
        }
    }

    @Override
    public SPAAlphabet<Integer> alphabet() {
        return this.alphabet;
    }

    @Override
    public Map<Integer, ? extends DFA<?, Integer>> automaton() {
        return this.automaton;
    }

    @Override
    public Integer initialProcedure() {
        return 0;
    }

    public static RandomSUL create(int size, int seed) {
        return new RandomSUL(size, seed);
    }
}

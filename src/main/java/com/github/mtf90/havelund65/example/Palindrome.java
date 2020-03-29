package com.github.mtf90.havelund65.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.mtf90.havelund65.impl.FastSPAAlphabet;
import com.github.mtf90.havelund65.api.Example;
import com.github.mtf90.havelund65.api.SPAAlphabet;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.automata.fsa.DFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class Palindrome implements Example<Character> {

    public static Palindrome INSTANCE = new Palindrome();

    @Override
    public SPAAlphabet<Character> alphabet() {
        final Alphabet<Character> callAlphabet = Alphabets.characters('S', 'T');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'c');

        return new FastSPAAlphabet<>(internalAlphabet, callAlphabet, 'R');
    }

    @Override
    public Map<Character, CompactDFA<Character>> automaton() {

        final SPAAlphabet<Character> alphabet = alphabet();

        final Set<Character> joinedSymbols = new HashSet<>();
        joinedSymbols.addAll(alphabet.getCallAlphabet());
        joinedSymbols.addAll(alphabet.getInternalAlphabet());

        final Alphabet<Character> joinedAlphabet = Alphabets.fromCollection(joinedSymbols);

        final CompactDFA<Character> sProcedure = AutomatonBuilders.newDFA(joinedAlphabet)
                                                                    .withInitial("s0")
                                                                    .withAccepting("s0", "s1", "s2", "s5")
                                                                    .from("s0").on('T').to("s5")
                                                                    .from("s0").on('a').to("s1")
                                                                    .from("s0").on('b').to("s2")
                                                                    .from("s1").on('S').to("s3")
                                                                    .from("s2").on('S').to("s4")
                                                                    .from("s3").on('a').to("s5")
                                                                    .from("s4").on('b').to("s5")
                                                                    .create();

        final CompactDFA<Character> tProcedure = AutomatonBuilders.newDFA(joinedAlphabet)
                                                                    .withInitial("t0")
                                                                    .withAccepting("t1", "t3")
                                                                    .from("t0").on('S').to("t3")
                                                                    .from("t0").on('c').to("t1")
                                                                    .from("t1").on('T').to("t2")
                                                                    .from("t2").on('c').to("t3")
                                                                    .create();

        final Map<Character, CompactDFA<Character>> subModels = new HashMap<>();
        subModels.put('S', DFAs.complete(sProcedure, joinedAlphabet));
        subModels.put('T', DFAs.complete(tProcedure, joinedAlphabet));

        return subModels;
    }

    @Override
    public Character initialProcedure() {
        return 'S';
    }
}


package com.github.mtf90.havelund65.api;

import java.util.Map;

import net.automatalib.automata.fsa.DFA;

public interface Example<I> {

    SPAAlphabet<I> alphabet();

    Map<I, ? extends DFA<?, I>> automaton();

    I initialProcedure();

}

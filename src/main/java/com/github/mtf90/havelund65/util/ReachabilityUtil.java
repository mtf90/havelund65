package com.github.mtf90.havelund65.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.graphs.ShortestPaths;

public class ReachabilityUtil {

    public static <I> boolean[][] canReachAcceptingState(List<DFA<?, I>> dfas, Collection<I> alphabet) {

        final boolean[][] outer = new boolean[dfas.size()][];

        int idx = 0;
        for (DFA<?, I> dfa : dfas) {
            outer[idx++] = canReachAcceptingState(dfa, alphabet);
        }

        return outer;
    }

    public static <S, I> boolean[] canReachAcceptingState(DFA<S, I> dfa, Collection<I> alphabet) {

        final StateIDs<S> ids = dfa.stateIDs();
        final boolean[] inner = new boolean[dfa.size()];

        for (int i = 0; i < dfa.size(); i++) {
            final Predicate<S> pred = dfa::isAccepting;
            inner[i] =
                    ShortestPaths.shortestPath(dfa.transitionGraphView(alphabet), ids.getState(i), dfa.size(), pred) !=
                    null;
        }

        return inner;

    }

}

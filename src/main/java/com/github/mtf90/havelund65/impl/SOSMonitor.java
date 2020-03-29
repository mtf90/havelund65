package com.github.mtf90.havelund65.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.mtf90.havelund65.api.SPAAlphabet;
import com.github.mtf90.havelund65.util.ReachabilityUtil;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.impl.Alphabets;

public class SOSMonitor<S, I> {

    final SPAAlphabet<I> alphabet;
    final I initialProcedure;

    final boolean[][] acceptanceMap;
    final DFA<S, I>[] dfas;
    private Configuration<S, I> configuration;

    private boolean inError;
    private long numReturns;

    public SOSMonitor(SPAAlphabet<I> alphabet, Map<I, ? extends DFA<S, I>> spa, I initialProcedure) {
        this.alphabet = alphabet;
        this.initialProcedure = initialProcedure;

        final int numProcs = alphabet.getNumCalls();
        dfas = new DFA[numProcs];

        final Set<I> proceduralSymbols = new HashSet<>();
        proceduralSymbols.addAll(alphabet.getCallAlphabet());
        proceduralSymbols.addAll(alphabet.getInternalAlphabet());

        for (int i = 0; i < numProcs; i++) {
            final DFA<S, I> dfa = spa.get(alphabet.getCallSymbol(i));
            dfas[i] = dfa;
        }

        this.acceptanceMap = ReachabilityUtil.canReachAcceptingState(Arrays.asList(dfas),
                                                                     Alphabets.fromCollection(proceduralSymbols));
    }

    public boolean monitor(I symbol) {

        if (inError) {
            return false;
        }

        final int procId;

        final StackElement<S, I> topOfStack;
        final I currentProcedure;
        final S currentState;

        // first symbol
        if (configuration == null) {
            if (!Objects.equals(symbol, initialProcedure)) {
                inError = true;
                return false;
            }

            procId = alphabet.getCallSymbolIndex(initialProcedure);
            final S init = dfas[procId].getInitialState();

            final StackElement<S, I> element = new StackElement<>(initialProcedure, init);

            configuration = new Configuration<>();
            configuration.stack.add(element);
            configuration.globalSymbolCount = 1;
            configuration.currentSymbolCounter = 1;
            configuration.maxSymbolCount = 1;

            return true;
        } else {
            topOfStack = configuration.stack.element();
            currentProcedure = topOfStack.procedure;
            currentState = topOfStack.state;

            procId = alphabet.getCallSymbolIndex(currentProcedure);
        }

        final DFA<S, I> dfa = dfas[procId];

        if (alphabet.isCallSymbol(symbol)) {
            final StackElement<S, I> stackElement = new StackElement<>(symbol, dfa.getInitialState());
            configuration.stack.push(stackElement);
        } else if (alphabet.isInternalSymbol(symbol)) {
            topOfStack.state = dfa.getSuccessor(currentState, symbol);
            configuration.stack.element().symbolCount++;
        } else if (alphabet.isReturnSymbol(symbol)) {

            if (!dfa.isAccepting(currentState)) {
                inError = true;
                reportStatistics();
                return false;
            }

            final StackElement<S, I> top = configuration.stack.pop();

            if (configuration.stack.isEmpty()) {
                inError = true;
            } else {
                final StackElement<S, I> newTopOfStack = configuration.stack.element();

                newTopOfStack.state = dfas[alphabet.getCallSymbolIndex(newTopOfStack.procedure)].getSuccessor(
                        newTopOfStack.state,
                        top.procedure);

                // count the procedural invocation we returned from
                configuration.stack.element().symbolCount++;
                // remove one more, since adding 1 below will correctly re-add the procedural call again
                configuration.currentSymbolCounter -= (top.symbolCount + 1);
            }
            this.numReturns++;

        } else {
            throw new IllegalArgumentException("Unexpected symbol '" + symbol + "' for " + alphabet);
        }

        configuration.globalSymbolCount++;
        configuration.currentSymbolCounter++;
        configuration.maxSymbolCount = Math.max(configuration.maxSymbolCount, configuration.currentSymbolCounter);
        return true;
    }

    public Stats reportStatistics() {
        return new Stats(configuration.globalSymbolCount,
                         configuration.maxSymbolCount,
                         configuration.stack.stream().mapToLong(se -> se.symbolCount).sum() + configuration.stack.size(),
                         configuration.stack.isEmpty() ? 0 : configuration.stack.element().symbolCount,
                         configuration.stack.size(),
                         numReturns);
    }

    @Deprecated
    public I currProc() {
        return this.configuration.stack.element().procedure;
    }

    @Deprecated
    public S currState() {
        return this.configuration.stack.element().state;
    }

    public static class Stats {

        public final long globalSymbolCount;
        public final long maxResourceConsumption;
        public final long reproductionLength;
        public final long localCELength;
        public final long stackHeight;
        public final long numReturns;

        public Stats(long globalSymbolCount,
                     long maxResourceConsumption,
                     long reproductionLength,
                     long localCELength,
                     long stackHeight,
                     long numReturns) {
            this.globalSymbolCount = globalSymbolCount;
            this.maxResourceConsumption = maxResourceConsumption;
            this.reproductionLength = reproductionLength;
            this.localCELength = localCELength;
            this.stackHeight = stackHeight;
            this.numReturns = numReturns;
        }
    }

}

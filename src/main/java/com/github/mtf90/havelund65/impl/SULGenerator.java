package com.github.mtf90.havelund65.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.github.mtf90.havelund65.api.SPAAlphabet;
import com.google.common.base.Preconditions;
import com.github.mtf90.havelund65.util.ReachabilityUtil;
import net.automatalib.automata.concepts.StateIDs;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.words.Alphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SULGenerator<S, I> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SULGenerator.class);

    private final SPAAlphabet<I> alphabet;
    private final I initialProcedure;

    private final DFA<S, I>[] dfas;
    private final StateIDs<S>[] ids;

    private final double procWeight;
    private final Random random;

    private boolean firstSymbol;

    private final Deque<StackElement<I, S>> stack;

    private List<I>[][] admissibleCallSymbols;
    private List<I>[][] admissibleInternalSymbols;

    public SULGenerator(SPAAlphabet<I> alphabet,
                        Map<I, ? extends DFA<S, I>> spa,
                        I initialProcedure,
                        double procWeight,
                        Random random) {
        Preconditions.checkArgument(procWeight >= 0);

        this.alphabet = alphabet;
        this.initialProcedure = initialProcedure;

        this.procWeight = procWeight;
        this.random = random;

        final int numProcs = alphabet.getNumCalls();
        dfas = new DFA[numProcs];
        ids = new StateIDs[numProcs];

        admissibleCallSymbols = new List[numProcs][];
        admissibleInternalSymbols = new List[numProcs][];

        final Set<I> proceduralSymbols = new HashSet<>();
        proceduralSymbols.addAll(alphabet.getCallAlphabet());
        proceduralSymbols.addAll(alphabet.getInternalAlphabet());

        for (int i = 0; i < numProcs; i++) {
            final DFA<S, I> dfa = spa.get(alphabet.getCallSymbol(i));
            final StateIDs<S> id = dfa.stateIDs();

            dfas[i] = dfa;
            ids[i] = id;

            boolean[] acceptance = ReachabilityUtil.canReachAcceptingState(dfa, proceduralSymbols);
            admissibleCallSymbols[i] = getAdmissibleSymbols(dfa, id, alphabet.getCallAlphabet(), acceptance);
            admissibleInternalSymbols[i] = getAdmissibleSymbols(dfa, id, alphabet.getInternalAlphabet(), acceptance);
        }

        this.firstSymbol = true;
        this.stack = new ArrayDeque<>();
    }

    public I generate() {

        if (firstSymbol) {
            firstSymbol = false;

            final StackElement<I, S> stackElement = new StackElement<>();
            stackElement.proc = this.initialProcedure;
            stackElement.state = dfas[this.alphabet.getCallSymbolIndex(this.initialProcedure)].getInitialState();

            this.stack.add(stackElement);
            return this.initialProcedure;
        }

        final StackElement<I, S> topOfStack = this.stack.peek();
        final I currProc = topOfStack.proc;
        final S currState = topOfStack.state;

        final int procId = this.alphabet.getCallSymbolIndex(currProc);
        final DFA<S, I> dfa = dfas[procId];
        final StateIDs<S> id = ids[procId];

        final List<I> possibleCallSymbols = admissibleCallSymbols[procId][id.getStateId(currState)];
        final List<I> possibleInternalSymbols = admissibleInternalSymbols[procId][id.getStateId(currState)];

        final int internalScore = possibleInternalSymbols.size();
        final double callScore = possibleCallSymbols.size() * procWeight;
        final double returnScore;

        if (dfa.isAccepting(currState) && stack.size() > 1) {
            returnScore = stack.size();
        } else {
            returnScore = 0;
        }

        final double totalScore = internalScore + callScore + returnScore;

        if (totalScore == 0) { // no symbol possible
            throw new GenerationException("Unable to perform an action");
        }

        final I result;
        final double rand = this.random.nextDouble();
        final double callThreshold = callScore / totalScore;
        final double returnThreshold = (returnScore / totalScore) + callThreshold;

        if (rand < callThreshold) { // call action
            result = RandomUtil.choose(possibleCallSymbols, this.random);
            final int resultId = this.alphabet.getCallSymbolIndex(result);

            topOfStack.state = dfa.getSuccessor(currState, result);

            final StackElement<I, S> newStack = new StackElement<>();
            newStack.proc = result;
            newStack.state = dfas[resultId].getInitialState();
            this.stack.push(newStack);
        } else if (rand < returnThreshold) { // return action
            this.stack.pop();
            return this.alphabet.getReturnSymbol();
        } else { // internal action
            result = RandomUtil.choose(possibleInternalSymbols, this.random);
            topOfStack.state = dfa.getSuccessor(currState, result);
        }

        return result;
    }

    private List<I>[] getAdmissibleSymbols(DFA<S, I> dfa, StateIDs<S> id, Alphabet<I> alphabet, boolean[] acceptance) {
        final List<I>[] result = new List[dfa.size()];

        for (final S s : dfa) {
            final int idx = id.getStateId(s);
            final List<I> possibleSymbols = new ArrayList<>(alphabet.size());
            for (final I i : alphabet) {
                final S succ = dfa.getSuccessor(s, i);
                if (acceptance[id.getStateId(succ)]) {
                    possibleSymbols.add(i);
                }
            }
            result[idx] = possibleSymbols;
        }

        return result;
    }

    @Deprecated
    public I currProc() {
        return this.stack.element().proc;
    }

    @Deprecated
    public S currState() {
        return this.stack.element().state;
    }

    private static class StackElement<I, S> {

        I proc;
        S state;
    }

    public static class GenerationException extends RuntimeException {

        GenerationException(String message) {
            super(message);
        }
    }
}

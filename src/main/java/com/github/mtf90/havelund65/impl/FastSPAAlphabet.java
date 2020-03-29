package com.github.mtf90.havelund65.impl;

import java.util.Collection;

import javax.annotation.Nullable;

import com.github.mtf90.havelund65.api.SPAAlphabet;
import net.automatalib.words.Alphabet;
import net.automatalib.words.abstractimpl.AbstractAlphabet;
import net.automatalib.words.impl.Alphabets;

public class FastSPAAlphabet<I> extends AbstractAlphabet<I> implements SPAAlphabet<I> {

    private final Alphabet<I> internalAlphabet;
    private final Alphabet<I> callAlphabet;
    private final Alphabet<I> returnAlphabet;

    public FastSPAAlphabet(Alphabet<I> internalSymbols, Alphabet<I> callSymbols, I returnSymbol) {
        this.internalAlphabet = internalSymbols;
        this.callAlphabet = callSymbols;
        this.returnAlphabet = Alphabets.singleton(returnSymbol);
    }

    @Override
    public Collection<I> getCallSymbols() {
        return callAlphabet;
    }

    @Override
    public Collection<I> getInternalSymbols() {
        return internalAlphabet;
    }

    @Override
    public Collection<I> getReturnSymbols() {
        return returnAlphabet;
    }

    @Override
    public int getNumCalls() {
        return callAlphabet.size();
    }

    @Override
    public int getNumInternals() {
        return internalAlphabet.size();
    }

    @Override
    public int getNumReturns() {
        return returnAlphabet.size();
    }

    @Override
    public SymbolType getSymbolType(I i) {
        if (internalAlphabet.containsSymbol(i)) {
            return SymbolType.INTERNAL;
        } else if (callAlphabet.containsSymbol(i)) {
            return SymbolType.CALL;
        } else if (returnAlphabet.containsSymbol(i)) {
            return SymbolType.RETURN;
        } else {
            return null;
        }
    }

    @Override
    public boolean isCallSymbol(I symbol) {
        return callAlphabet.containsSymbol(symbol);
    }

    @Override
    public boolean isInternalSymbol(I symbol) {
        return internalAlphabet.containsSymbol(symbol);
    }

    @Override
    public boolean isReturnSymbol(I symbol) {
        return returnAlphabet.containsSymbol(symbol);
    }

    @Override
    public Alphabet<I> getCallAlphabet() {
        return callAlphabet;
    }

    @Override
    public I getCallSymbol(int i) throws IllegalArgumentException {
        return callAlphabet.getSymbol(i);
    }

    @Override
    public int getCallSymbolIndex(I i) throws IllegalArgumentException {
        return callAlphabet.getSymbolIndex(i);
    }

    @Override
    public Alphabet<I> getInternalAlphabet() {
        return internalAlphabet;
    }

    @Override
    public I getInternalSymbol(int i) throws IllegalArgumentException {
        return internalAlphabet.getSymbol(i);
    }

    @Override
    public int getInternalSymbolIndex(I i) throws IllegalArgumentException {
        return internalAlphabet.getSymbolIndex(i);
    }

    @Override
    public Alphabet<I> getReturnAlphabet() {
        return returnAlphabet;
    }

    @Override
    public I getReturnSymbol(int i) throws IllegalArgumentException {
        return returnAlphabet.getSymbol(i);
    }

    @Override
    public int getReturnSymbolIndex(I i) throws IllegalArgumentException {
        return returnAlphabet.getSymbolIndex(i);
    }

    @Nullable
    @Override
    public I getSymbol(int i) throws IllegalArgumentException {
        int localIndex = i;

        if (localIndex < internalAlphabet.size()) {
            return internalAlphabet.getSymbol(localIndex);
        } else {
            localIndex -= internalAlphabet.size();
        }

        if (localIndex < callAlphabet.size()) {
            return callAlphabet.getSymbol(localIndex);
        } else {
            localIndex -= callAlphabet.size();
        }

        if (localIndex < returnAlphabet.size()) {
            return returnAlphabet.getSymbol(localIndex);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int getSymbolIndex(@Nullable I i) throws IllegalArgumentException {
        int offset = 0;

        if (internalAlphabet.containsSymbol(i)) {
            return internalAlphabet.getSymbolIndex(i);
        } else {
            offset += internalAlphabet.size();
        }

        if (callAlphabet.containsSymbol(i)) {
            return offset + callAlphabet.getSymbolIndex(i);
        } else {
            offset += callAlphabet.size();
        }

        if (returnAlphabet.containsSymbol(i)) {
            return offset + returnAlphabet.getSymbolIndex(i);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int size() {
        return internalAlphabet.size() + callAlphabet.size() + returnAlphabet.size();
    }
}

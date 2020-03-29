package com.github.mtf90.havelund65.impl;

import java.util.ArrayDeque;
import java.util.Deque;

public class Configuration<S, I> {

    Deque<StackElement<S, I>> stack;

    long globalSymbolCount;
    long currentSymbolCounter;
    long maxSymbolCount;

    public Configuration() {
        this.stack = new ArrayDeque<>();
    }
}

package com.github.mtf90.havelund65.impl;

public class StackElement<S, I> {

    I procedure;
    S state;

    long symbolCount;

    public StackElement(I procedure, S state) {
        this.procedure = procedure;
        this.state = state;
    }
}

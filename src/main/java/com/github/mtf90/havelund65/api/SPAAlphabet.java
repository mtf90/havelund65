package com.github.mtf90.havelund65.api;

import net.automatalib.words.VPDAlphabet;

public interface SPAAlphabet<I> extends VPDAlphabet<I> {

    default I getReturnSymbol() {
        return getReturnAlphabet().getSymbol(0);
    }

}

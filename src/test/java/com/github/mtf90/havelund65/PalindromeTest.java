package com.github.mtf90.havelund65;

import java.util.Map;

import com.github.mtf90.havelund65.api.SPAAlphabet;
import com.github.mtf90.havelund65.example.Palindrome;
import com.github.mtf90.havelund65.impl.SOSMonitor;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PalindromeTest {

    final Palindrome ex = Palindrome.INSTANCE;
    final SPAAlphabet<Character> alphabet = ex.alphabet();
    final Map<Character, CompactDFA<Character>> procedures = ex.automaton();

    @Test
    public void testComplete() {

        final Word<Character> accepted = Word.fromCharSequence("SaSTcRRaR");
        final SOSMonitor<?, Character> m = new SOSMonitor<>(alphabet, procedures, 'S');

        accepted.forEach(i -> Assert.assertTrue(m.monitor(i)));
        Assert.assertFalse(m.monitor('a'));
    }

    @Test
    public void testPartial1() {

        final Word<Character> accepted = Word.fromCharSequence("SbST");
        final SOSMonitor<?, Character> m = new SOSMonitor<>(alphabet, procedures, 'S');

        accepted.forEach(i -> Assert.assertTrue(m.monitor(i)));
        Assert.assertTrue(m.monitor('a'));
        Assert.assertTrue(m.monitor('b'));
        Assert.assertTrue(m.monitor('c'));
        Assert.assertFalse(m.monitor('R'));
    }

    @Test
    public void testPartial2() {

        final Word<Character> accepted = Word.fromCharSequence("SbST");
        final SOSMonitor<?, Character> m = new SOSMonitor<>(alphabet, procedures, 'S');

        accepted.forEach(i -> Assert.assertTrue(m.monitor(i)));
        Assert.assertTrue(m.monitor('c'));
        Assert.assertTrue(m.monitor('R'));
        Assert.assertTrue(m.monitor('a'));
        Assert.assertFalse(m.monitor('R'));
    }
}

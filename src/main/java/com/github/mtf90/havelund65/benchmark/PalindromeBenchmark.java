package com.github.mtf90.havelund65.benchmark;

import com.github.mtf90.havelund65.example.Palindrome;

public class PalindromeBenchmark {

    public static void main(String[] args) {
        final Runner<Character> runner = new Runner<>(Palindrome.INSTANCE, 1, (long) 1e1, 2, 42);
        runner.run();
    }
}

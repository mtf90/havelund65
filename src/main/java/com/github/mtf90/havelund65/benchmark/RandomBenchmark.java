package com.github.mtf90.havelund65.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.mtf90.havelund65.example.RandomSUL;

public class RandomBenchmark {

    private static final double[] WEIGHTS = new double[] {0.01, 0.1, 0.5, 1, 2, 5};
    private static final int RUNS = 25;
    private static final int PROC_SIZE = 50;
    private static final int LOG_FROM = (int) 1e0;

     private static long MAXSIZE = (long) 1e9;
//    private static long MAXSIZE = (long) 1e6;

    public static void main(String[] args) {

        final List<Runner<Integer>> runners = new ArrayList<>(RUNS * WEIGHTS.length);
        final Random r = new Random(42);

        for (int run = 0; run < RUNS; run++) {
            RandomSUL sul = RandomSUL.create(PROC_SIZE, run);

            for (double weight : WEIGHTS) {
                runners.add(new Runner<>(sul, LOG_FROM, MAXSIZE, weight, r.nextInt()));
            }
        }
        runners.parallelStream().forEach(Runner::run);
    }
}

package com.skronawi.DescentDiceCompanion.lib.random;

import java.util.Random;

public class LocalRandomness implements Randomness {

    private Random r;

    public LocalRandomness() {
        r = new Random();
    }

    @Override
    public int nextInt(int upper) {
        return r.nextInt(upper);
    }

}

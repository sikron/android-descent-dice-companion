package com.skronawi.DescentDiceCompanion.app.random;

import com.skronawi.DescentDiceCompanion.app.main.MainActivity;
import com.skronawi.DescentDiceCompanion.lib.random.LocalRandomness;
import com.skronawi.DescentDiceCompanion.lib.random.Randomness;

public class RandomnessProvider {

    private static Randomness local;
    private static Randomness external;

    public static void init(MainActivity main) {
        local = new LocalRandomness();
        external = new RandomOrgAsynchronousRandomness(main);
    }

    public static Randomness getLocalRandomness() {
        return local;
    }

    public static Randomness getExternalRandomness() {
        return external;
    }
}

package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.common.utils.RandomUtils;

public class DNA {
    private static final float MUTATION_CHANCE = 0.02f;

    final Gene[] genes = new Gene[16];

    DNA() {
        for (Gene gene : genes) {
            gene.setRandom();
        }
    }

    private DNA(DNA original) {
        set(original);
    }

    public DNA copy() {
        return new DNA(this);
    }

    public void set(DNA original) {
        for (int i = 0; i < 16; ++i) {
            this.genes[i].set(original.genes[i]);
        }
    }

    public void mutate() {
        final RandomUtils.RandomGen rand = Gene.RAND;
        for (Gene gene : genes) {
            if (rand.nextBool(MUTATION_CHANCE)) {
                gene.setRandom();
            }
        }
    }

    public void reset() {
        for (Gene gene : genes) {
            gene.reset();
        }
    }
}

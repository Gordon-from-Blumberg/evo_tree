package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.Gdx;
import com.gordonfromblumberg.games.core.common.utils.RandomUtils;

public class DNA {
    static final int COLOR = 16;
    static final int GENES_COUNT = 17;
    private static final float MUTATION_CHANCE = 0.02f;

    final Gene[] genes = new Gene[GENES_COUNT];

    DNA() {
        for (int i = 0; i < GENES_COUNT; ++i) {
            genes[i] = new Gene();
            genes[i].setRandom();
        }
    }

    public void set(DNA original) {
        for (int i = 0; i < GENES_COUNT; ++i) {
            this.genes[i].set(original.genes[i]);
        }
    }

    public void mutate() {
        final RandomUtils.RandomGen rand = Gene.RAND;
        for (Gene gene : genes) {
            if (rand.nextBool(MUTATION_CHANCE)) {
                gene.mutate();
                Gdx.app.log("DNA", "Gene has mutated");
            }
        }
    }

    public void reset() {
        for (Gene gene : genes) {
            gene.reset();
        }
    }
}

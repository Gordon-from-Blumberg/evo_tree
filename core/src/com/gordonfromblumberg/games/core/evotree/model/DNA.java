package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.Gdx;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomUtils;

public class DNA {
    static final int SEED_SPROUT_LIGHT = 0;
    static final int COLOR = 1;
    static final int LIFETIME = 2;

    static final int SPROUT_GENES_COUNT;
    static final int SPECIAL;
    public static final int GENES_COUNT;

    private static final float MUTATION_CHANCE;

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        SPROUT_GENES_COUNT = configManager.getInteger("dna.sproutGenesCount");
        int geneCount = SPROUT_GENES_COUNT;
        SPECIAL = geneCount++;
        GENES_COUNT = geneCount;

        MUTATION_CHANCE = configManager.getFloat("dna.mutationChance");
    }

    private final Gene[] genes = new Gene[GENES_COUNT];

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

    public Gene getGene(int index) {
        return genes[index];
    }

    Gene getSpecialGene(int valueIndex) {
        return genes[genes[SPECIAL].getValue(valueIndex) % SPROUT_GENES_COUNT];
    }

    public void reset() {
        for (Gene gene : genes) {
            gene.reset();
        }
    }
}

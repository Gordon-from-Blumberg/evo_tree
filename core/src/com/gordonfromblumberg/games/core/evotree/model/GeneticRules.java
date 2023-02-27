package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.IntSet;

public class GeneticRules {
    private final IntSet activeConditions = new IntSet(Condition.ALL.length);

    public GeneticRules() {
        for (Condition condition : Condition.ALL) {
            activeConditions.add(condition.value);
        }
    }

    public boolean hasActiveConditions() {
        return activeConditions.notEmpty();
    }

    public boolean isActiveCondition(byte value) {
        return activeConditions.contains(value);
    }
}

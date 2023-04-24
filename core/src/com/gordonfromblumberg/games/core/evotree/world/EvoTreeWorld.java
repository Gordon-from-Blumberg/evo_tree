package com.gordonfromblumberg.games.core.evotree.world;

import com.gordonfromblumberg.games.core.evotree.model.CellGrid;
import com.gordonfromblumberg.games.core.evotree.model.GeneticRules;
import com.gordonfromblumberg.games.core.evotree.model.Seed;
import com.gordonfromblumberg.games.core.evotree.model.Tree;

public interface EvoTreeWorld {
    void addSeed(Seed seed);
    void removeSeed(Seed seed);

    void addTree(Tree tree);

    CellGrid getGrid();
    GeneticRules getGeneticRules();
}

package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public class TreePart extends LivingCellObject {
    private static final Logger log = LogManager.create(TreePart.class);
    private static final Pool<TreePart> pool = new Pool<TreePart>() {
        @Override
        protected TreePart newObject() {
            return new TreePart();
        }
    };
    private static final ObjectSet<Gene> PROCESSED_GENES = new ObjectSet<>(DNA.SPROUT_GENES_COUNT);

    private static final int MIN_ABSORPTION = 4;
    private static final int MAX_ABSORPTION = 50;
    static final int ENERGY_CONSUMPTION = 10;

    Tree tree;
    TreePart parent;
    Array<TreePart> children = new Array<>(4);
    TreePartType type;
    Gene activeGene;
    int turnsToDisappear;

    private TreePart() {}

    public static TreePart getInstance() {
        return pool.obtain();
    }

    boolean update(CellGrid grid, Array<TreePart> newShoots, EvoTreeWorld world) {
        if (type == TreePartType.WOOD) {
            return false;
        }

        if (type == TreePartType.DEAD) {
            return --turnsToDisappear == 0;
        }

        GeneticRules rules = world.getGeneticRules();
        if (rules.hasActiveConditions()) {
            Gene gene = activeGene;
            while (!PROCESSED_GENES.contains(gene)) {
                PROCESSED_GENES.add(gene);

                int checkResult = 0;
                byte condition1 = gene.getValue(Gene.CONDITION1);
                if (rules.isActiveCondition(condition1)
                        && Condition.of(condition1).check(grid, this, gene.getValue(Gene.PARAMETER1)))
                    checkResult |= 1;
                byte condition2 = gene.getValue(Gene.CONDITION2);
                if (rules.isActiveCondition(condition2)
                        && Condition.of(condition2).check(grid, this, gene.getValue(Gene.PARAMETER2)))
                    checkResult |= 2;

                if (checkResult == 0) {
                    PROCESSED_GENES.clear();
                    activeGene = gene;
                    break;
                } else {
//                    log.debug("Conditions " + condition1 + ", " + condition2 + "; check res = " + checkResult);
                    byte action = gene.getValue(Gene.ACTION + checkResult - 1);
                    if (0 <= action && action < DNA.SPROUT_GENES_COUNT) {
                        gene = tree.dna.getGene(action);
                    } else if (rules.isActiveAction(action)) {
                        return Action.of(action).act(grid, this, world);
                    }
                }
            }
            PROCESSED_GENES.clear();
        }

        int requiredEnergy = calcSproutCost(grid);
        if (requiredEnergy < tree.energy) {
            tree.energy -= requiredEnergy;
            sprout(grid, newShoots);
            if (!isBlocked(grid)) {
                becomeWood();
            }
        }

        return false;
    }

    void sprout(CellGrid grid, Array<TreePart> newShoots) {
        Cell cell = this.cell;

        for (Direction dir : Direction.ALL) {
            int nextActiveGene = activeGene.getValue(dir);
            if (0 <= nextActiveGene && nextActiveGene < DNA.SPROUT_GENES_COUNT) {
                Cell neib = grid.getCell(cell, dir);
                if (neib != null && neib.object == null) {
                    TreePart shoot = getInstance();
                    shoot.type = TreePartType.SHOOT;
                    shoot.setCell(neib);
                    newShoots.add(shoot);
                    shoot.activeGene = tree.dna.getGene(nextActiveGene);
                    shoot.lightAbsorption = calcLightAbsorption(shoot.activeGene.getValue(Gene.LIGHT_ABSORPTION));
                    addChild(shoot);
                }
            }
        }
    }

    void becomeWood() {
        type = TreePartType.WOOD;
        activeGene = null;
        --tree.shootCount;
    }

    int calcSproutCost(CellGrid grid) {
        int result = 0;
        for (Direction dir : Direction.ALL) {
            int nextGene = activeGene.getValue(dir);
            if (0 <= nextGene && nextGene < DNA.SPROUT_GENES_COUNT) {
                Cell neib = grid.getCell(cell, dir);
                if (neib != null && neib.object == null) {
                    int x = calcLightAbsorption(tree.dna.getGene(nextGene).getValue(Gene.LIGHT_ABSORPTION)) - 4;
                    result += x * x / 2 + 4;
//                    result += 3 * calcLightAbsorption(tree.dna.getGene(nextGene).getValue(Gene.LIGHT_ABSORPTION));
                }
            }
        }
        return result;
    }

    boolean isBlocked(CellGrid grid) {
        for (Direction dir : Direction.ALL) {
            if (activeGene.getValue(dir) < DNA.SPROUT_GENES_COUNT) {
                Cell neib = grid.getCell(cell, dir);
                if (neib == null && dir == Direction.up
                        || neib != null && neib.object != null
                        && !(neib.object instanceof TreePart && ((TreePart) neib.object).tree == this.tree)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isBlocked(CellGrid grid, byte dirs, boolean toSprout) {
        byte dirFlag = 1;
        for (Direction dir : Direction.ALL) {
            if ((dirs & dirFlag) == dirFlag && (!toSprout || activeGene.getValue(dir) < DNA.SPROUT_GENES_COUNT)) {
                Cell neib = grid.getCell(cell, dir);
                if (neib == null || neib.getObject() != null) {
                    return true;
                }
            }
            dirFlag <<= 1;
        }
        return false;
    }

    public int calcAbsorbedLight(CellGrid grid) {
        return Math.min(calcLight(grid), getLightAbsorption());
    }

    public int calcEnergy(CellGrid grid) {
        return 2 * calcAbsorbedLight(grid);
    }

    protected static int calcLightAbsorption(int geneValue) {
        int absorption = geneValue < 0 ? MIN_ABSORPTION : MIN_ABSORPTION + geneValue;
        return absorption > MAX_ABSORPTION ? MAX_ABSORPTION : absorption;
    }

    public Tree getTree() {
        return tree;
    }

    void addChild(TreePart child) {
        child.parent = this;
        children.add(child);
    }

    void removeFromParent() {
        if (parent != null) {
            parent.children.removeValue(this, true);
            parent = null;
        }
    }

    public TreePartType getType() {
        return type;
    }

    @Override
    public void reset() {
        super.reset();

        tree = null;
        parent = null;
        children.clear();
        type = null;
        turnsToDisappear = 0;
    }

    @Override
    public void release() {
        pool.free(this);
    }
}

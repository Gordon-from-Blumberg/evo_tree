package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;

public class TreePart extends LivingCellObject {
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

    // conditions
    private static final byte FALSE_CONDITION = 0;
    private static final byte TRUE_CONDITION = 1;

    private static final byte SHOOT_HEIGHT_LESS = 2;
    private static final byte SHOOT_HEIGHT_EQUALS = 3;
    private static final byte SHOOT_HEIGHT_MORE = 4;
    private static final byte LIGHT_LESS = 5;
    private static final byte LIGHT_MORE = 6;
    private static final byte TREE_HEIGHT_LESS = 7;
    private static final byte TREE_HEIGHT_MORE = 8;
    private static final byte TREE_SIZE_LESS = 9;
    private static final byte TREE_SIZE_MORE = 10;
    private static final byte TREE_ENERGY_LESS = 11;
    private static final byte TREE_ENERGY_MORE = 12;
    private static final byte BRANCH_LENGTH_LESS = 13;
    private static final byte BRANCH_LENGTH_EQUALS = 14;
    private static final byte BRANCH_LENGTH_MORE = 15;
    private static final byte TREE_LIFETIME_LESS = 16;
    private static final byte TREE_LIFETIME_MORE = 17;
    private static final byte IS_BLOCKED_TO_SPROUT = 18;
    private static final byte IS_NOT_BLOCKED_TO_SPROUT = 19;
    private static final byte IS_BLOCKED = 20;
    private static final byte IS_NOT_BLOCKED = 21;

    private static final byte MIN_CONDITION = FALSE_CONDITION;
    private static final byte MAX_CONDITION = IS_NOT_BLOCKED;

    Tree tree;
    TreePartType type;
    Gene activeGene;

    private TreePart() {
        super(ENERGY_CONSUMPTION);
    }

    public static TreePart getInstance() {
        return pool.obtain();
    }

    boolean update(CellGrid grid, Array<TreePart> newShoots) {
        Gene gene = activeGene;
        PROCESSED_GENES.add(gene);
        while (shouldCheckCondition(gene)
                && checkCondition(gene.getValue(Gene.CONDITION1), gene.getValue(Gene.PARAMETER1), grid)
                && checkCondition(gene.getValue(Gene.CONDITION2), gene.getValue(Gene.PARAMETER2), grid)) {
            int geneValue = gene.getValue(Gene.MOVE_TO);
            if (0 <= geneValue && geneValue < DNA.SPROUT_GENES_COUNT) {
                Gene next = tree.dna.getGene(geneValue);
                if (!PROCESSED_GENES.contains(next)) {
                    gene = next;
                    PROCESSED_GENES.add(next);
                } else {
                    activeGene = gene;
                    PROCESSED_GENES.clear();
                    return false;
                }
            } else {
                PROCESSED_GENES.clear();
                return false;
            }
        }
        PROCESSED_GENES.clear();

        activeGene = gene;

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
                }
            }
        }
    }

    private void becomeWood() {
        type = TreePartType.WOOD;
        activeGene = null;
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

    private boolean shouldCheckCondition(Gene gene) {
        return false;
//        byte condition1 = gene.getValue(Gene.CONDITION1);
//        byte condition2 = gene.getValue(Gene.CONDITION2);
//        return condition1 >= MIN_CONDITION && condition1 <= MAX_CONDITION
//                || condition2 >= MIN_CONDITION && condition2 <= MAX_CONDITION;
    }

    private boolean checkCondition(byte condition, byte parameter, CellGrid grid) {
        final byte DIRS_MOD = (byte) (1 << Direction.ALL.length);
        switch (condition) {
            case FALSE_CONDITION:
                return false;
            case TRUE_CONDITION:
                return true;
            case SHOOT_HEIGHT_LESS:
                return cell.y < parameter;
            case SHOOT_HEIGHT_EQUALS:
                return cell.y == parameter;
            case SHOOT_HEIGHT_MORE:
                return cell.y > parameter;
            case LIGHT_LESS:
                return calcLight(grid) <= parameter;
            case LIGHT_MORE:
                return calcLight(grid) > parameter;
            case TREE_HEIGHT_LESS:
                return tree.getHeight() < parameter;
            case TREE_HEIGHT_MORE:
                return tree.getHeight() > parameter;
            case TREE_SIZE_LESS:
                return tree.getSize() < parameter * 2;
            case TREE_SIZE_MORE:
                return tree.getSize() > parameter * 2;
            case TREE_ENERGY_LESS:
                return tree.energy < parameter * 300;
            case TREE_ENERGY_MORE:
                return tree.energy > parameter * 300;
            case BRANCH_LENGTH_LESS:
                return Math.abs(cell.x - tree.root.x) < parameter / 2;
            case BRANCH_LENGTH_EQUALS:
                return Math.abs(cell.x - tree.root.x) == parameter / 2;
            case BRANCH_LENGTH_MORE:
                return Math.abs(cell.x - tree.root.x) > parameter / 2;
            case TREE_LIFETIME_LESS:
                return tree.lifetime < parameter;
            case TREE_LIFETIME_MORE:
                return tree.lifetime > parameter;
            case IS_BLOCKED_TO_SPROUT:
                return isBlocked(grid, (byte) (parameter % DIRS_MOD), true);
            case IS_NOT_BLOCKED_TO_SPROUT:
                return !isBlocked(grid, (byte) (parameter % DIRS_MOD), true);
            case IS_BLOCKED:
                return isBlocked(grid, (byte) (parameter % DIRS_MOD), false);
            case IS_NOT_BLOCKED:
                return !isBlocked(grid, (byte) (parameter % DIRS_MOD), false);
        }
        return true;
    }

    public int calcEnergy(CellGrid grid) {
        return 2 * calcLight(grid);
    }

    protected static int calcLightAbsorption(int geneValue) {
        int absorption = geneValue < 0 ? MIN_ABSORPTION : MIN_ABSORPTION + geneValue;
        return absorption > MAX_ABSORPTION ? MAX_ABSORPTION : absorption;
    }

    public Tree getTree() {
        return tree;
    }

    public TreePartType getType() {
        return type;
    }

    @Override
    public void reset() {
        super.reset();

        tree = null;
        type = null;
    }

    @Override
    public void release() {
        pool.free(this);
    }
}

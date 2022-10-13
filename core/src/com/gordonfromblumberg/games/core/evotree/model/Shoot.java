package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;

public class Shoot extends Wood {
    private static final Pool<Shoot> pool = new Pool<Shoot>() {
        @Override
        protected Shoot newObject() {
            return new Shoot();
        }
    };
    private static final ObjectSet<Gene> PROCESSED_GENES = new ObjectSet<>(DNA.SPROUT_GENES_COUNT);

    private static final byte FALSE_CONDITION = 32;
    private static final byte TRUE_CONDITION = 33;

    private static final byte SHOOT_HEIGHT_LESS = 34;
    private static final byte SHOOT_HEIGHT_EQUALS = 35;
    private static final byte SHOOT_HEIGHT_MORE = 36;
    private static final byte LIGHT_LESS = 37;
    private static final byte LIGHT_MORE = 38;

    private static final byte MIN_CONDITION = FALSE_CONDITION;
    private static final byte MAX_CONDITION = LIGHT_MORE;

    Gene activeGene;

    private Shoot() {}

    public static Shoot getInstance() {
        return pool.obtain();
    }

    boolean update(CellGrid grid, Array<Shoot> newShoots) {
        Gene gene = activeGene;
        PROCESSED_GENES.add(gene);
        while (shouldCheckCondition(gene)
                && checkCondition(gene.getValue(Gene.CONDITION1), gene.getValue(Gene.PARAMETER1), grid)
                && checkCondition(gene.getValue(Gene.CONDITION2), gene.getValue(Gene.PARAMETER2), grid)) {
            if (gene.getValue(Gene.MOVE_TO) < DNA.SPROUT_GENES_COUNT) {
                Gene next = tree.dna.getGene(gene.getValue(Gene.MOVE_TO));
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
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    void sprout(CellGrid grid, Array<Shoot> newShoots) {
        Cell cell = this.cell;

        for (Direction dir : Direction.ALL) {
            int nextActiveGene = activeGene.getValue(dir);
            if (nextActiveGene < DNA.SPROUT_GENES_COUNT) {
                Cell neib = grid.getCell(cell, dir);
                if (neib != null && neib.treePart == null) {
                    Shoot shoot = getInstance();
                    shoot.setCell(neib);
                    newShoots.add(shoot);
                    shoot.activeGene = tree.dna.getGene(nextActiveGene);
                    shoot.lightAbsorption = calcLightAbsorption(shoot.activeGene.getValue(Gene.LIGHT_ABSORPTION));
                }
            }
        }
    }

    private void becomeWood() {
        Wood wood = Wood.getInstance();
        wood.setCell(cell);
        wood.lightAbsorption = this.lightAbsorption;
        tree.addWood(wood);
    }

    int calcSproutCost(CellGrid grid) {
        int result = 0;
        for (Direction dir : Direction.ALL) {
            int nextGene = activeGene.getValue(dir);
            if (nextGene < DNA.SPROUT_GENES_COUNT) {
                Cell neib = grid.getCell(cell, dir);
                if (neib != null && neib.treePart == null) {
                    result += 3 * calcLightAbsorption(tree.dna.getGene(nextGene).getValue(Gene.LIGHT_ABSORPTION));
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
                        || neib != null && neib.treePart != null
                        && !(neib.treePart instanceof Wood && ((Wood) neib.treePart).tree == this.tree)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldCheckCondition(Gene gene) {
        byte condition1 = gene.getValue(Gene.CONDITION1);
        byte condition2 = gene.getValue(Gene.CONDITION2);
        return condition1 >= MIN_CONDITION && condition1 <= MAX_CONDITION
                || condition2 >= MIN_CONDITION && condition2 <= MAX_CONDITION;
    }

    private boolean checkCondition(byte condition, byte parameter, CellGrid grid) {
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
        }
        return true;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        super.reset();

        activeGene = null;
    }
}

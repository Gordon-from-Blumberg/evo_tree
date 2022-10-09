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

    private static final byte SHOOT_HEIGHT_LESS = 17;
    private static final byte SHOOT_HEIGHT_EQUALS = 18;
    private static final byte SHOOT_HEIGHT_MORE = 19;
    private static final byte LIGHT_LESS = 20;
    private static final byte LIGHT_MORE = 21;

    private static final byte FALSE_CONDITION = Gene.MAX_VALUE - 2;

    Gene activeGene;

    private Shoot() {}

    public static Shoot getInstance() {
        return pool.obtain();
    }

    boolean update(CellGrid grid, Array<Shoot> newShoots) {
        Gene gene = activeGene;
        PROCESSED_GENES.add(gene);
        while (checkCondition(gene.getValue(Gene.CONDITION1), gene.getValue(Gene.PARAMETER1), grid)
                && checkCondition(gene.getValue(Gene.CONDITION2), gene.getValue(Gene.PARAMETER2), grid)) {
            if (gene.getValue(Gene.MOVE_TO) < DNA.SPROUT_GENES_COUNT) {
                Gene next = tree.dna.getGene(gene.getValue(Gene.MOVE_TO));
                if (!PROCESSED_GENES.contains(next)) {
                    gene = next;
                    PROCESSED_GENES.add(next);
//                    Gdx.app.log("SHOOT", "Gene was moved by conditions!");
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
        if (requiredEnergy < tree.energy && !isBlocked(grid)) {
            tree.energy -= requiredEnergy;
            sprout(grid, newShoots);
            return true;
        }

        return false;
    }

    void sprout(CellGrid grid, Array<Shoot> newShoots) {
        Cell cell = this.cell;
        becomeWood();

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

    private boolean checkCondition(byte condition, byte parameter, CellGrid grid) {
        switch (condition) {
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
        return condition < FALSE_CONDITION;
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

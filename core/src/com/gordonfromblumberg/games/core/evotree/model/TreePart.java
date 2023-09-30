package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
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

    private static final int MIN_ABSORPTION;
    private static final int MAX_ABSORPTION;
    private static final int ABSORPTION_SHIFT;
    static final int ENERGY_CONSUMPTION = 10;
    private static final int[] PUSH_SEED_COST = new int[] { 5, 3, 0, 3 };

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        MIN_ABSORPTION = configManager.getInteger("treePart.minAbsorption");
        MAX_ABSORPTION = configManager.getInteger("treePart.maxAbsorption");
        ABSORPTION_SHIFT = configManager.getInteger("treePart.absorptionShift");
    }

    Tree tree;
    TreePart parent;
    Array<TreePart> children = new Array<>(4);
    TreePartType type;
    Gene activeGene;
    int turnsToDisappear;
    final DNA buffer = new DNA();
    boolean isBufferFilled = false;

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

        if (calcLight(grid) >= lightToDie()) {
            die();
            return false;
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

//        int requiredEnergy = calcSproutCost(grid);
//        if (requiredEnergy < tree.energy) {
//            tree.energy -= requiredEnergy;
//            sprout(grid, newShoots);
//            if (!isBlocked(grid)) {
//                becomeWood();
//            }
//        }

        if (sprout(grid, newShoots)) {
            becomeWood();
        }

        return false;
    }

    boolean sprout(CellGrid grid, Array<TreePart> newShoots) {
        Cell cell = this.cell;
        boolean sprouted = false;

        for (Direction dir : Direction.ALL) {
            int nextActiveGene = activeGene.getValue(dir);
            if (0 <= nextActiveGene && nextActiveGene < DNA.SPROUT_GENES_COUNT) {
                int seedsToPush = countSeedsToPush(grid, dir, cell);
                if (seedsToPush == -1)
                    continue;

                int requiredEnergy = PUSH_SEED_COST[dir.getCode()] * seedsToPush + calcSproutCost(nextActiveGene);
                if (requiredEnergy >= tree.energy)
                    continue;

                tree.energy -= requiredEnergy;
                sprouted = true;

                Cell neib = grid.getCell(cell, dir);

                Cell targetCell = grid.getCell(neib, dir);
                CellObject seedToPush = neib.object;
                CellObject nextSeed = targetCell != null ? targetCell.object : null;
                while (seedsToPush-- > 0 && targetCell != null) {
                    grid.moveCellObjectTo(seedToPush, targetCell);
                    targetCell = grid.getCell(targetCell, dir);
                    seedToPush = nextSeed;
                    nextSeed = targetCell != null ? targetCell.object : null;
                }

                TreePart shoot = getInstance();
                shoot.type = TreePartType.SHOOT;
                grid.addCellObject(shoot, neib);
                newShoots.add(shoot);
                shoot.activeGene = tree.dna.getGene(nextActiveGene);
                shoot.lightAbsorption = calcLightAbsorption(shoot.activeGene.getValue(Gene.LIGHT_ABSORPTION));
                shoot.buffer.set(this.buffer);
                shoot.isBufferFilled = this.isBufferFilled;
                addChild(shoot);
            }
        }

        return sprouted;
    }

    int countSeedsToPush(CellGrid grid, Direction dir, Cell shootCell) {
        int count = 0;
        Cell cell = shootCell;
        while ((cell = grid.getCell(cell, dir)) != null) {
            CellObject object = cell.object;
            if (object == null)
                return count;
            if (!(object instanceof Seed))
                return -1;
            ++count;
        }
        return -1;
    }

    int lightToDie() {
        return (120 - lightAbsorption + ABSORPTION_SHIFT) * 3;
    }

    void die() {
        type = TreePartType.DEAD;
        for (TreePart child : children) {
            child.die();
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
//                    int x = calcLightAbsorption(tree.dna.getGene(nextGene).getValue(Gene.LIGHT_ABSORPTION)) - 4;
//                    result += x * x / 2 + 4;
//                    result += 3 * calcLightAbsorption(tree.dna.getGene(nextGene).getValue(Gene.LIGHT_ABSORPTION));
                    result += calcSproutCost(nextGene);
                }
            }
        }
        return result;
    }

    int calcSproutCost(int nextGene) {
        return 4 * (calcLightAbsorption(tree.dna.getGene(nextGene).getValue(Gene.LIGHT_ABSORPTION)));
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
        return Math.min(calcLight(grid), getLightAbsorption() - ABSORPTION_SHIFT);
    }

    public int calcEnergy(CellGrid grid) {
        return 2 * calcAbsorbedLight(grid);
    }

    protected static int calcLightAbsorption(int geneValue) {
        int absorption = geneValue < 0 ? MIN_ABSORPTION : MIN_ABSORPTION + geneValue;
        return (absorption > MAX_ABSORPTION ? MAX_ABSORPTION : absorption) + ABSORPTION_SHIFT;
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
        buffer.reset();
        isBufferFilled = false;
    }

    @Override
    public void release() {
        pool.free(this);
    }
}

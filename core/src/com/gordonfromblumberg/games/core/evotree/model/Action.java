package com.gordonfromblumberg.games.core.evotree.model;

import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

public enum Action {
    DO_NOTHING((byte) -1) {
        @Override
        boolean act(CellGrid grid, TreePart treePart, EvoTreeWorld world) {
            return false;
        }
    },
    BECOME_SEED((byte) -2) {
        @Override
        boolean act(CellGrid grid, TreePart treePart, EvoTreeWorld world) {
            final int requiredEnergy = 10;
            final Tree tree = treePart.tree;
            int seedEnergy = tree.energy / tree.shootCount;
            if (seedEnergy > Tree.MAX_ENERGY_PER_SEED) seedEnergy = Tree.MAX_ENERGY_PER_SEED;
            if (tree.energy > requiredEnergy + seedEnergy) {
                tree.energy -= requiredEnergy + seedEnergy;
                --tree.shootCount;
                Seed seed = tree.createSeed(seedEnergy, treePart.cell);
                world.addSeed(seed);
                return true;
            }
            return false;
        }
    },
    DROP_SEED((byte) -3) {
        private final Direction[] priorityDirs = new Direction[] {Direction.down, Direction.left, Direction.right, Direction.up};
        @Override
        boolean act(CellGrid grid, TreePart treePart, EvoTreeWorld world) {
            final int requiredEnergy = 20;
            final Tree tree = treePart.tree;
            int seedEnergy = (tree.energy / tree.shootCount) / 2;
            if (seedEnergy > Tree.MAX_ENERGY_PER_SEED) seedEnergy = Tree.MAX_ENERGY_PER_SEED;
            if (tree.energy > requiredEnergy + seedEnergy) {
                tree.energy -= requiredEnergy + seedEnergy;
                for (Direction dir : priorityDirs) {
                    Cell seedCell = grid.getCell(treePart.cell, dir);
                    if (seedCell != null && seedCell.object == null) {
                        Seed seed = tree.createSeed(seedEnergy, seedCell);
                        world.addSeed(seed);
                        break;
                    }
                }
            }
            return false;
        }
    },
    BECOME_WOOD((byte) -4) {
        @Override
        boolean act(CellGrid grid, TreePart treePart, EvoTreeWorld world) {
            treePart.becomeWood();
            return false;
        }
    },
    DECREASE_TREE_LIFETIME((byte) -5) {
        @Override
        boolean act(CellGrid grid, TreePart treePart, EvoTreeWorld world) {
            treePart.tree.lifetime -= 2;
            return false;
        }
    },
    DIE((byte) -6) {
        @Override
        boolean act(CellGrid grid, TreePart treePart, EvoTreeWorld world) {
            TreePart parent = treePart.parent;
            if (parent != null && parent.type == TreePartType.WOOD) {
                parent.type = TreePartType.SHOOT;
                parent.activeGene = treePart.activeGene;
            } else {
                --treePart.tree.shootCount;
            }
            return true;
        }
    }
    ;

    static final Action[] ALL = values();
    final byte value;

    Action(byte value) {
        this.value = value;
    }

    static Action of(byte value) {
        return ALL[-1 - value];
    }

    abstract boolean act(CellGrid grid, TreePart treePart, EvoTreeWorld world);
}

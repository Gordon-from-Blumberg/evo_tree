package com.gordonfromblumberg.games.core.evotree.model;

public enum Condition {
    FALSE((byte) -1) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return false;
        }
    },
    TRUE((byte) -2) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return true;
        }
    },
    SHOOT_HEIGHT_LESS((byte) -3) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.cell.y < parameter;
        }
    },
    SHOOT_HEIGHT_EQUALS((byte) -4) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.cell.y == parameter;
        }
    },
    SHOOT_HEIGHT_MORE((byte) -5) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.cell.y > parameter;
        }
    },
    LIGHT_LESS((byte) -6) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.calcLight(grid) <= parameter;
        }
    },
    LIGHT_MORE((byte) -7) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.calcLight(grid) > parameter;
        }
    },
    TREE_HEIGHT_LESS((byte) -8) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.tree.getHeight() < parameter;
        }
    },
    TREE_HEIGHT_MORE((byte) -9) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.tree.getHeight() > parameter;
        }
    },
    TREE_SIZE_LESS((byte) -10) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.tree.getSize() < parameter * 2;
        }
    },
    TREE_SIZE_MORE((byte) -11) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.tree.getSize() > parameter * 2;
        }
    },
    TREE_ENERGY_LESS((byte) -12) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.tree.energy < parameter * 300;
        }
    },
    TREE_ENERGY_MORE((byte) -13) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return treePart.tree.energy > parameter * 300;
        }
    },
    BRANCH_LENGTH_LESS((byte) -14) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return Math.abs(treePart.cell.x - treePart.tree.root.x) < parameter / 2;
        }
    },
    BRANCH_LENGTH_EQUALS((byte) -15) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return Math.abs(treePart.cell.x - treePart.tree.root.x) == parameter / 2;
        }
    },
    BRANCH_LENGTH_MORE((byte) -16) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return Math.abs(treePart.cell.x - treePart.tree.root.x) > parameter / 2;
        }
    },
    TREE_LIFETIME_LESS((byte) -17) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return  treePart.tree.lifetime < parameter;
        }
    },
    TREE_LIFETIME_MORE((byte) -18) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return  treePart.tree.lifetime > parameter;
        }
    },
    IS_BLOCKED_TO_SPROUT((byte) -19) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return  treePart.isBlocked(grid, (byte) (parameter % DIRS_MOD), true);
        }
    },
    IS_NOT_BLOCKED_TO_SPROUT((byte) -20) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return  !treePart.isBlocked(grid, (byte) (parameter % DIRS_MOD), true);
        }
    },
    IS_BLOCKED((byte) -21) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return  treePart.isBlocked(grid, (byte) (parameter % DIRS_MOD), false);
        }
    },
    IS_NOT_BLOCKED((byte) -22) {
        @Override
        boolean check(CellGrid grid, TreePart treePart, byte parameter) {
            return  !treePart.isBlocked(grid, (byte) (parameter % DIRS_MOD), false);
        }
    }
    ;

    static final Condition[] ALL = values();
    private static final byte DIRS_MOD = (byte) (1 << Direction.ALL.length);
    final byte value;

    Condition(byte value) {
        this.value = value;
    }

    static Condition of(byte value) {
        return ALL[-1 - value];
    }

    abstract boolean check(CellGrid grid, TreePart treePart, byte parameter);
}

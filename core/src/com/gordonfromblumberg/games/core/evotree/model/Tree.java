package com.gordonfromblumberg.games.core.evotree.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import com.gordonfromblumberg.games.core.evotree.world.EvoTreeWorld;

import java.util.Iterator;

public class Tree implements Poolable {
    private static final Pool<Tree> pool = new Pool<Tree>() {
        @Override
        protected Tree newObject() {
            return new Tree();
        }
    };
    private static final Logger log = LogManager.create(Tree.class);

    static final int MAX_ENERGY_PER_SEED;
    private static final float MIN_COLOR_VALUE;
    private static final float COLOR_DIFF;
    private static final int MIN_LIFETIME;
    private static final int MAX_LIFETIME;
    private static final int POLLEN_SPREAD_RADIUS;
    private static final float POLLINATE_CHANCE;

    static {
        ConfigManager configManager = AbstractFactory.getInstance().configManager();
        MAX_ENERGY_PER_SEED = configManager.getInteger("tree.maxEnergyPerSeed");
        MIN_COLOR_VALUE = configManager.getFloat("tree.minColor");
        COLOR_DIFF = configManager.getFloat("tree.maxColor") - MIN_COLOR_VALUE;
        MIN_LIFETIME = configManager.getInteger("tree.minLifetime");
        MAX_LIFETIME = configManager.getInteger("tree.maxLifetime");
        POLLEN_SPREAD_RADIUS = configManager.getInteger("tree.pollenSpreadRadius");
        POLLINATE_CHANCE = configManager.getFloat("tree.pollinateChance");
    }

    private static final Array<TreePart> newShoots = new Array<>();
    private static final Array<CellObject> cellObjects = new Array<>();

    int id;
    int generation;
    int lifetime;
    int age;
    final DNA dna = new DNA();
    int energy;
    Cell root;
    int maxHeight;
    final Array<TreePart> treeParts = new Array<>();
    int shootCount; // is updated inside #update()
    private final Color color = new Color();

    boolean justSprouted;
    boolean isDead;

    private Tree() {}

    public static Tree getInstance() {
        return pool.obtain();
    }

    public void init() {
        Gene gene = dna.getSpecialGene(DNA.COLOR);
        int r = Math.max(gene.getValue(0) ^ gene.getValue(1), 0);
        int g = Math.max(gene.getValue(1) ^ gene.getValue(2), 0);
        int b = Math.max(gene.getValue(2) ^ gene.getValue(3), 0);
        color.set(
                MIN_COLOR_VALUE + COLOR_DIFF * r / Gene.MAX_VALUE,
                MIN_COLOR_VALUE + COLOR_DIFF * g / Gene.MAX_VALUE,
                MIN_COLOR_VALUE + COLOR_DIFF * b / Gene.MAX_VALUE,
                1
        );
        Gene treeLifetime = dna.getSpecialGene(DNA.LIFETIME);
        int lifetime = 0;
        for (int i = 0; i < Gene.VALUE_COUNT; ++i) {
            int value = treeLifetime.getValue(i);
            if (value > 0)
                lifetime += value;
        }
        this.lifetime = (lifetime + 1) % (MAX_LIFETIME - MIN_LIFETIME) + MIN_LIFETIME;
        this.age = 0;
    }

    /**
     * Updates state of this tree
     * @param world Game world
     * @return true if this tree should be removed from world
     */
    public boolean update(EvoTreeWorld world) {
        if (justSprouted) {
            justSprouted = false;
            return false;
        }

        CellGrid grid = world.getGrid();
        if (!isDead) {
            if (++age >= lifetime) {
                produceSeeds(world);
                die();
                return false;
            }

            int newEnergy = energy;
            for (TreePart treePart : treeParts) {
                if (treePart.getType() != TreePartType.DEAD) {
                    newEnergy += treePart.calcEnergy(grid) - TreePart.ENERGY_CONSUMPTION;
                }
            }
            energy = newEnergy;

            if (energy <= 0) {
                log.debug("Tree #" + id + " has no energy and dies");
                die();
                return false;
            }
        }

        Iterator<TreePart> it = treeParts.iterator();
        while (it.hasNext()) {
            TreePart part = it.next();
            if (part.update(grid, newShoots, world)) {
                it.remove();
                part.removeFromParent();
                grid.removeCellObject(part);
                part.release();
            }
        }
        for (TreePart newShoot : newShoots) {
            addPart(newShoot);
        }
        newShoots.clear();

        return treeParts.isEmpty();
    }

    public void addPart(TreePart part) {
        treeParts.add(part);
        part.tree = this;
        if (part.cell.y > maxHeight) {
            maxHeight = part.cell.y;
        }
        int max = getRestLifeTime();
        if (max < 1) max = 1;
        int min = max / 2;
        if (min < 1) min = 1;
        part.turnsToDisappear = RandomGen.INSTANCE.nextInt(min, max);
        if (part.type == TreePartType.SHOOT) {
            ++shootCount;
        }
    }

    Seed createSeed(int energy, Cell cell, CellGrid grid, TreePart origin) {
        Seed seed = Seed.getInstance();
        grid.addCellObject(seed, cell);
        seed.generation = generation + 1;
        if (origin.isBufferFilled)
            seed.dna.set(this.dna, origin.buffer);
        else
            seed.dna.set(this.dna);
        seed.dna.mutate();
        seed.init();
        seed.energy = energy;
        return seed;
    }

    private void produceSeeds(EvoTreeWorld world) {
        log.info("Tree #" + id + " attempts to produce seeds: energy=" + energy + ", shoots=" + shootCount);
        if (energy >= shootCount && shootCount > 0) {
            CellGrid grid = world.getGrid();
            int energyPerSeed = (energy / shootCount) + 1;
            if (energyPerSeed > MAX_ENERGY_PER_SEED) {
                energyPerSeed = MAX_ENERGY_PER_SEED;
            }
            int nextGeneration = generation + 1;
            Iterator<TreePart> it = treeParts.iterator();
            int pollenRadius = energyPerSeed * POLLEN_SPREAD_RADIUS / MAX_ENERGY_PER_SEED;
            log.debug("Pollen radius = " + pollenRadius);
            Cell top = null, left = null, right = null;
            while (it.hasNext()) {
                TreePart part = it.next();
                if (part.type == TreePartType.SHOOT) {
                    Seed seed = createSeed(energyPerSeed, part.cell, grid, part);
                    world.addSeed(seed);
                    log.info("Seed #" + seed.id + " was produced by tree #" + id
                            + " with energy " + energyPerSeed + " of gen " + nextGeneration);
                    if (top == null) {
                        top = left = right = part.cell;
                    } else {
                        top = compareCells(top, part.cell, Direction.up, grid.width);
                        left = compareCells(top, part.cell, Direction.left, grid.width);
                        right = compareCells(top, part.cell, Direction.right, grid.width);
                    }
                    it.remove();
                    part.removeFromParent();
                    part.release();
                }
            }
            if (top != null) {
                pollinate(grid, top, pollenRadius);
                if (left != top) pollinate(grid, left, pollenRadius);
                if (right != top && right != left) pollinate(grid, right, pollenRadius);
            }
        }
    }

    private Cell compareCells(Cell old, Cell newCell, Direction dir, int gridWidth) {
        int kx = 0;
        switch (dir) {
            case up:
                return old.y < newCell.y ? newCell : old;
            case left:
                kx = -1;
                break;
            case right:
                kx = 1;
                break;
        }
        int oldX = old.x;
        if (Math.abs(oldX - newCell.x) > 100) {
            oldX = oldX > newCell.x ? oldX - gridWidth : oldX + gridWidth;
        }
        return 2 * old.y + kx * oldX < 2 * newCell.y + kx * newCell.x ? newCell : old;
    }

    private void pollinate(CellGrid grid, Cell shootCell, int radius) {
        Array<CellObject> array = cellObjects;

        int dx = radius + shootCell.y;
        // top cell inside grid
        if (dx < grid.height) {
            // left line
            array.addAll(grid.findObjectsUnderLine(shootCell.x - dx, 0, shootCell.x, dx));
            // right line
            array.addAll(grid.findObjectsUnderLine(shootCell.x, dx, shootCell.x + dx, 0));

        // top cell above grid
        } else {
            int dy = dx - grid.height + 1;
            int topY = grid.height - 1;
            // left line
            array.addAll(grid.findObjectsUnderLine(shootCell.x - dx, 0, shootCell.x - dy, topY));
            // right line
            array.addAll(grid.findObjectsUnderLine(shootCell.x + dy, topY,shootCell.x + dx, 0));
            // middle line
            array.addAll(grid.findObjectsUnderLine(shootCell.x - dy + 1, topY,shootCell.x + dy - 1, topY));
        }

        int n = 0;
        for (CellObject cellObject : array) {
            if (cellObject instanceof TreePart) {
                TreePart treePart = (TreePart) cellObject;
                if (treePart.type == TreePartType.SHOOT
                        && !treePart.isBufferFilled
                        && treePart.tree != this
                        && RandomGen.INSTANCE.nextBool(POLLINATE_CHANCE)) {
                    treePart.buffer.set(dna);
                    treePart.isBufferFilled = true;
                    ++n;
                }
            }
        }
        log.debug("From shoot at " + shootCell.x + ", " + shootCell.y + " " + n + " shoots have been pollinated");

        array.clear();
    }

    private void die() {
        isDead = true;
        for (TreePart part : treeParts) {
            part.type = TreePartType.DEAD;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return treeParts.size;
    }

    public Color getColor() {
        return color;
    }

    public int getEnergy() {
        return energy;
    }

    public int getHeight() {
        return maxHeight - root.y + 1;
    }

    public int getGeneration() {
        return generation;
    }

    public DNA getDna() {
        return dna;
    }

    public int getLifetime() {
        return lifetime;
    }

    public int getAge() {
        return age;
    }

    public int getRestLifeTime() {
        return lifetime - age;
    }

    public boolean isDead() {
        return isDead;
    }

    public int getShootCount() {
        return shootCount;
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        id = 0;
        generation = 0;
        lifetime = 0;
        age = 0;
        dna.reset();
        energy = 0;
        root = null;
        maxHeight = 0;
        for (TreePart part : treeParts) {
            part.release();
        }
        treeParts.clear();
        shootCount = 0;
        color.set(0);
        isDead = false;
    }
}

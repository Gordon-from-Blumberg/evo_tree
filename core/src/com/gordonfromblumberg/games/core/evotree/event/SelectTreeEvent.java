package com.gordonfromblumberg.games.core.evotree.event;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.evotree.model.Tree;

public class SelectTreeEvent implements Event {
    private final static Pool<SelectTreeEvent> pool = new Pool<SelectTreeEvent>() {
        @Override
        protected SelectTreeEvent newObject() {
            return new SelectTreeEvent();
        }
    };

    private Tree tree;

    private SelectTreeEvent() {}

    public static SelectTreeEvent getInstance() {
        return pool.obtain();
    }

    public Tree getTree() {
        return tree;
    }

    public SelectTreeEvent setTree(Tree tree) {
        this.tree = tree;
        return this;
    }

    @Override
    public String getType() {
        return "selectTree";
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        tree = null;
    }
}

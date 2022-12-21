package com.gordonfromblumberg.games.core.common.event;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

@SuppressWarnings("unchecked")
public class EventProcessor {
    private static final Logger log = LogManager.create(EventProcessor.class);

    private final ObjectMap<String, Array<EventHandler>> handlers = new IdentityMap<>(16);
    private final Queue<Event> eventQueue = new Queue<>();

    public void registerHandler(String type, EventHandler handler) {
        Array<EventHandler> handlerList;
        if (!handlers.containsKey(type)) {
            handlerList = new Array<>(8);
            handlers.put(type, handlerList);
        } else {
            handlerList = handlers.get(type);
        }

        handlerList.add(handler);
    }

    public void push(Event event) {
        eventQueue.addLast(event);
    }

    public void process() {
        final Queue<Event> queue = eventQueue;
        while (queue.notEmpty()) {
            final Event event = queue.removeFirst();
            final Array<EventHandler> handlerList = handlers.get(event.getType());

            if (Main.DEBUG)
                log.debug("Process event '" + event.getType() + "', registered handlers " + handlerList.size);

            if (handlerList != null) {
                for (EventHandler handler : handlerList) {
                    if (handler.handle(event)) {
                        break;
                    }
                }
            }
            event.release();
        }
    }
}

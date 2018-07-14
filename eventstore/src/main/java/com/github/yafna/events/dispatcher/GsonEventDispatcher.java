package com.github.yafna.events.dispatcher;

import com.github.yafna.events.store.EventStore;
import com.google.gson.Gson;

import java.util.concurrent.ForkJoinPool;

/**
 * A version of EventDispatcher that uses Gson to serialize and deserialize events. 
 */
public class GsonEventDispatcher extends EventDispatcher {
    private final Gson gson = new Gson();

    public GsonEventDispatcher(EventStore store) {
        this(store, new ForkJoinPool());
    }

    public GsonEventDispatcher(EventStore store, ForkJoinPool executor) {
        super(store, executor);
    }

    @Override
    protected <T> String serialize(T event) {
        return gson.toJson(event);
    }

    @Override
    protected <T> T deserialize(String payload, Class<T> clazz) {
        return gson.fromJson(payload, clazz);
    }
}

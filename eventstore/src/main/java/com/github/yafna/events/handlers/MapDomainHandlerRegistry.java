package com.github.yafna.events.handlers;

import com.github.yafna.events.aggregate.PayloadUtils;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * A trivial handler registry, backed up by a map.
 * Provides neat generic signatures and encapsulates the map to prevent its modification.
 * Does not provide any validation features - it should be done before instantiating it.
 * 
 * @param <A> aggregate type
 */
@AllArgsConstructor
public class MapDomainHandlerRegistry<A> implements DomainHandlerRegistry<A> {
    private final Map<String, List<DomainHandler<A, ?>>> handlers;

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<DomainHandler<A, T>> get(Class<T> clazz) {
        return (List<DomainHandler<A, T>>) (Object) handlers.get(PayloadUtils.eventType(clazz).value());
    }
}

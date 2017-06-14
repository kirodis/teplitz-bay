package com.github.yafna.events;

import com.github.yafna.events.aggregate.AggregateUtils;
import com.github.yafna.events.annotations.EvType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * Event that was not persisted yet. Designed to be returned as emitted in event handlers. 
 * Implementing this as a separate class provides hard compile-time guarantee that no event 
 * could be handled before it was persisted. * 
 */
@AllArgsConstructor
@Getter
public class EmittedEvent<T> {
    final String origin;
    final String aggregateId;
    final String type;
    final T payload;

    public static EmittedEvent of(String origin, String aggregateId, String type) {
        return new EmittedEvent<>(origin, aggregateId, type, null);
    }

    public static <A, T extends DomainEvent<A>> EmittedEvent<T> of(String aggregateId, T payload) {
        EvType evType = AggregateUtils.eventType(payload.getClass());
        String explicitOrigin = evType.origin();
        String origin = Objects.equals("", explicitOrigin) ? AggregateUtils.origin(payload.getClass()) : explicitOrigin;
        String type = evType.value();
        return new EmittedEvent<>(origin, aggregateId, type, payload);
    }
}

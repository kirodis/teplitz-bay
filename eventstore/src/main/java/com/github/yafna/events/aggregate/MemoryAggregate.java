package com.github.yafna.events.aggregate;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Aggregate which only keeps state in memory.
 * This class should be extended by most pure domain objects.
 * While other implementation are not currently provided, it is important to keep it separate from the interface
 * to ensure that legacy classes can implement it instead of changing hierarchy.
 */
public abstract class MemoryAggregate implements Aggregate {
    @Getter
    private AtomicLong lastEvent = new AtomicLong(-1);

}

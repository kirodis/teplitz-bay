package com.github.yafna.events.handlers;

import com.github.yafna.events.Event;

/**
 * Only handlers of this type of event are allowed to update domain models.
 * However they are not allowed to emit further events (unlike {@link EventHandler}).
 *
 * @param <A> Origin type
 * @param <T> Event type
 */
public interface DomainHandler<A, T> {

    /**
     * Applies event to aggregate. 
     * The implementation is free to mutate and return the original object or create a new instance.
     * While the latter approach is considered to be cleaner from multi-threaded perspective, 
     * it is often impractical with larger objects, especially if some fields are normal java collections.
     * Of course, you can use some 'functional' Java library (such as vavr.io) to get immutable collections,
     * howeve we don't expect everybody who might use our library to do so.
     * 
     * @param object domain object to which state is applied
     * @param meta metadata of the event being processed
     * @param payload event payload
     */
    A apply(A object, Event meta, T payload);
}

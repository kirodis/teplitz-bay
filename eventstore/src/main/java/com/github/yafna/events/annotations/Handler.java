package com.github.yafna.events.annotations;

import com.github.yafna.events.Event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks methods of aggregate object that describe event handlers.
 * Such methods must match one of the following contracts:
 *
 * <p> 1. One argument, {@link Event} (which carries event metadata).
 * No attempt to read payload is made when invoking it.
 *
 * <p> 2. One argument, event payload. Argument type must be assignment compatible with event payload type as
 *
 * <p> 3. Two arguments, first is event metadata, second is event payload.
 *
 * <p> Note that event payload argument type must be assignment compatible with EvType event payload type as
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Handler {
    /**
     * Event name, in case multiple events are described by a single payload class,
     * or payload argument type does not math event payload type exactly.
     */
    String[] value() default {};
}

package com.github.yafna.events.handlers.count;

import com.github.yafna.events.EmittedEvent;
import com.github.yafna.events.Event;
import com.github.yafna.events.annotations.Handler;
import com.github.yafna.events.handlers.event.EventHandler;
import com.github.yafna.events.rabbits.RabbitAdded;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Handler
public class RabbitCountingHandler implements EventHandler<RabbitAdded> {
    AtomicLong count = new AtomicLong(0);
    
    @Override
    public Stream<EmittedEvent<?>> apply(Event meta, RabbitAdded payload) {
        long v = count.incrementAndGet();
        RabbitNumberIsEven cool = new RabbitNumberIsEven(v, "cool");
        if (v % 2 == 0) { 
            return Stream.of(EmittedEvent.of(Rabbits.ID, cool)); 
        } else { 
            return Stream.of();
        }
    }
}

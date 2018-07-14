package com.github.yafna.events.aggregate;

/**
 * Ensures that exactly one instance of aggregate exists 
 */
public class SingletonAggregate extends MemoryAggregate {
    public final static String ID = "0000";
    
    @Override
    public final String getId() {
        return ID;  
    }
}

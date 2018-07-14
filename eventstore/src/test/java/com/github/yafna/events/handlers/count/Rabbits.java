package com.github.yafna.events.handlers.count;

import com.github.yafna.events.aggregate.SingletonAggregate;
import com.github.yafna.events.annotations.Handler;
import com.github.yafna.events.annotations.Origin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Origin("rabbits")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Rabbits extends SingletonAggregate {
    public final static Rabbits INSTANCE = new Rabbits(); 
    
    @Getter
    private String lastMessage;

    @Handler
    public void create(RabbitNumberIsEven payload) {
        lastMessage = payload.getMessage();
        log.info("Odd numbers are [{}]", payload.getMessage());
    }
}

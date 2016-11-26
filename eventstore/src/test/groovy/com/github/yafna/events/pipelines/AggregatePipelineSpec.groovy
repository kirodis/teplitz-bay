package com.github.yafna.events.pipelines

import com.github.yafna.events.Event
import com.github.yafna.events.utils.EventScanner
import com.github.yafna.events.handlers.DomainHandlerRegistry
import com.github.yafna.events.rabbits.Rabbit
import com.github.yafna.events.rabbits.RabbitAdded
import com.github.yafna.events.rabbits.RabbitNameUpdated
import com.github.yafna.events.store.file.GsonFileEventStore
import spock.lang.Specification

import java.time.Clock

class AggregatePipelineSpec extends Specification {

    private GsonFileEventStore store = new GsonFileEventStore(Clock.systemUTC(), File.createTempDir())

    Map<String, Class<?>> index = EventScanner.events(Rabbit)
    DomainHandlerRegistry<Rabbit> handlers = EventScanner.handlers(Rabbit)

    @SuppressWarnings("UnnecessaryQualifiedReference")
    def "push"() {
        given:
            AggregatePipeline<Rabbit> subj = new AggregatePipeline(Rabbit.class, store, index, handlers, { new Rabbit(it) })
        when:
            Event added = subj.push("ABCD-1234", new RabbitAdded("Kirk", "Captain's key"))
        then:
            added.payload == '{"name":"Kirk","publicKey":"Captain\\u0027s key"}'
        when:
            Rabbit kirk = subj.get("ABCD-1234")
        then:
            kirk.name == "Kirk"
            kirk.publicKey == "Captain's key"
        when:
            Event nameUpdated = subj.push("ABCD-1234", new RabbitNameUpdated("Scotty"))
        then:
            nameUpdated.payload == '{"name":"Scotty"}'
        when:
            Rabbit scotty = subj.get("ABCD-1234")
        then:
            scotty.name == "Scotty"
    }


}

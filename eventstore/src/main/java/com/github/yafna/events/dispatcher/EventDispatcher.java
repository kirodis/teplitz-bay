package com.github.yafna.events.dispatcher;

import com.github.yafna.events.EmittedEvent;
import com.github.yafna.events.Event;
import com.github.yafna.events.EventMeta;
import com.github.yafna.events.annotations.EvType;
import com.github.yafna.events.store.EventStore;
import com.github.yafna.events.store.ProtoEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Manages serialization and deserialization of events, dispatches emitted events from pipelines to event store
 * and notifies pipelines about stored events. Since each pipeline refers exactly one dispatcher, pipelines that
 * refer to different dispatchers backed up by different stores are independent.
 * This is useful when multiple message flows need to be isolated from each other inside one application.
 *
 * Application classes should not deal with {@link EventDispatcher} directly, but refer to pipelines instead.
 */
@AllArgsConstructor
@Slf4j
public abstract class EventDispatcher {
    private final static String UNKNOWN_TYPE = "Unknown type";

    private final EventStore store;
    private final ExecutorService executor;

    public <T> Event store(String origin, String aggregateId, T event) {
        String type = event.getClass().getAnnotation(EvType.class).value();
        String json = serialize(event);
        return store.persist(origin, aggregateId, type, json);
    }

    public long store(String causeId, String corrId, Stream<EmittedEvent> events) {
        ProtoEvent[] protoEvents = events.map(event -> {
            String json = serialize(event.getPayload());
            return new ProtoEvent(event.getOrigin(), event.getAggregateId(), event.getType(), json);
        }).toArray(ProtoEvent[]::new);
        // Array used here to be sure that all the events are successfully serialized at this point
        return store.persist(causeId, corrId, protoEvents);
    }

    public Stream<EventMeta<?>> getEvents(String origin, String aggregateId, Long fromSeq, Map<String, Class<?>> index) {
        return store.getEvents(origin, aggregateId, fromSeq).<EventMeta<?>>map(event -> {
            String type = event.getType();
            Class<?> clazz = index.get(type);
            if (clazz == null) {
                log.error("Event #{} [{}->{}] ignored: [{}]", event.getId(), origin, type, UNKNOWN_TYPE);
                String knownTypes = index.keySet().stream().collect(Collectors.joining(","));
                log.warn("Known types for [{}] are:\n{}", origin, knownTypes);
                return null;
            } else {
                return new EventMeta<>(event, deserialize(event.getPayload(), clazz));
            }
        }).filter(Objects::nonNull);
    }

    public <T> Stream<EventMeta<T>> subscribe(String origin, String type, Instant since, Class<T> clazz, BiConsumer<Event, T> callback) {
        return Optional.ofNullable(
                store.subscribe(origin, type, since, asyncCallback(clazz, callback))
        ).map(
                spliterator -> StreamSupport.stream(spliterator, false).map(
                        event -> new EventMeta<>(event, deserialize(event.getPayload(), clazz))
                )
        ).orElse(null);
    }

    private <T> Consumer<Event> asyncCallback(Class<T> clazz, BiConsumer<Event, T> callback) {
        return event -> executor.submit(
                () -> callback.accept(event, deserialize(event.getPayload(), clazz))
        );
    }

    protected abstract <T> String serialize(T event);

    protected abstract <T> T deserialize(String payload, Class<T> clazz);
}

package com.github.yafna.events.store;

import com.github.yafna.events.Event;

import java.time.Instant;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Abstraction for persisting events in external storage and reading them. 
 * This is an SPI to implement custom event persistence, such as DB.
 * This is NOT an API that application should call.
 * Under normal use cases, only {@link com.github.yafna.events.dispatcher.EventDispatcher} should be aware of this.
 */
public interface EventStore {
    /**
     * Reads events for a given aggregate instance.
     * 
     * @param origin aggregate name
     * @param aggregateId aggregate id
     * @param fromSeq event sequence number after which events should be returned
     * @return Sequential stream of events, ordered by sequence number.
     */
    Stream<Event> getEvents(String origin, String aggregateId, Long fromSeq);

    /**
     * Persists a single event.
     * 
     * @param origin aggregate type identifier
     * @param aggregateId aggregate id to which event is assiciated
     * @param type event type
     * @param payload serialized event payload
     * @return Persisted event. This allows caller to immediately access id, timestamp and other metadata of the event.  
     */
    Event persist(String origin, String aggregateId, String type, String payload);

    /**
     * Persists multiple events, adding causation id and correlation id.
     * If the implementation supports transactions, all events should be persisted in the same transaction
     * and any subscribers should be called only after the transaction is committed. 
     *
     * @param causeId id of the event that has caused events being persisted
     * @param corrId correlation id of the event that has caused events being persisted.
     * if it has {@code null} correlation id, event's 'normal' id should be used insstead
     * @param events events to per persisted 
     * @return Stream of event that were persisted  
     */
    long persist(String causeId, String corrId, ProtoEvent[] events);


    /**
     * Conditionally subscribes to events from a given moment.
     * If there are no events with matching origin and type present in store 
     * since the given instant, returns {@code null} and subscribes provided callback to be invoked 
     * when matching event has been persisted. 
     * If there are matching events present, the subscription is not set up and one or more 
     * of them will be returned. The returned events will contain at least all the events that 
     * happened *exactly* at a given instant, however there are no other guarantees regarding them.
     * 
     * The subscription callbacks are executed synchronously inside the #persist() method.
     * In most (but not all) of the cases it undesired to block repository for the duration of callback execution, 
     * therefore, it is the duty of the caller to provide anynchronous blocks.
     * When multiple subscribers are reacting to the same event, no assumptions can be made about the execution order.
     * 
     * @param origin origin to subscribe to
     * @param type subscription event type 
     * @param since the moment in time from which pas events are requested 
     * @param callback function to be invoked on each event.
     * @return List of events present in store since (non-inclusive) the given instant,   
     */
    Spliterator<Event> subscribe(String origin, String type, Instant since, Consumer<Event> callback);
}

package com.github.yafna.events.pipelines;

import com.github.yafna.events.DomainEvent;
import com.github.yafna.events.EmittedEvent;
import com.github.yafna.events.Event;
import com.github.yafna.events.EventMeta;
import com.github.yafna.events.aggregate.PayloadUtils;
import com.github.yafna.events.dispatcher.EventDispatcher;
import com.github.yafna.events.handlers.EventHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * Processes events using {@link EventHandler}. 
 * As such events can emit other events, they should not be reprocessed to prevent duplicating emitted events.
 * To ensure it, the events emitted by the handler are persisted together with the special "handled" 
 * event that registers the fact that processing is complete. 
 * 
 * When instantiated, pipeline connects to {@link EventDispatcher} and attempts to gathers all the events in the given
 * time interval that do not have a matching "handled" event.
 * TODO Implement that filtering, now pipeline ignores "handled" records 
 * 
 * @param <A> Aggregate type
 * @param <T> Event type
 */
@Slf4j
public class EventPipeline<A, T extends DomainEvent<A>> {
    /** Aggregate name under which service events recording state of the handler are saved to event store */
    public final static String AGGREGATE_NAME = "handlers";
    /** Event type used to mark handled events */
    public final static String EVENT_TYPE_HANDLED = "handled";

    private final ExecutorService executor;
    private final EventDispatcher dispatcher;
    private final String origin;
    private final String handlerId;
    private final EventHandler<T> handler;

    public EventPipeline(
            EventDispatcher dispatcher, Class<T> eventType, EventHandler<T> handler, String handlerId, Clock clock, ExecutorService executor
    ) {
        origin = PayloadUtils.origin(eventType);
        this.executor = executor;
        this.dispatcher = dispatcher;
        this.handler = handler;
        this.handlerId = handlerId;
        Duration timeWindow = Duration.ofDays(1);
        Instant since = clock.instant().minus(timeWindow);
        this.executor.submit(() -> recap(dispatcher, eventType, since));
    }

    private void recap(EventDispatcher dispatcher, Class<T> eventType, Instant start) {
        String type = PayloadUtils.eventType(eventType).value();

        for (Instant t = start; ; ) {
            Stream<EventMeta<T>> recap = dispatcher.subscribe(origin, type, t, eventType, this::process);
            if (recap == null) {
                break;
            } else {
                t = recap.map(
                        event -> process(event.getMeta(), event.getPayload())
                ).max(Comparator.naturalOrder()).orElseThrow(() -> {
                    log.error("Got empty stream for [{}].[{}]", origin, type);
                    return new IllegalStateException("Should never return empty stream");
                });
            }
        }
    }

    private Instant process(Event meta, T payload) {
        Stream<EmittedEvent> emitted = Stream.concat(
                handler.apply(meta, payload),
                Stream.of(EmittedEvent.of(AGGREGATE_NAME, handlerId, EVENT_TYPE_HANDLED))
        );
        String causeId = meta.getId();
        String corrId = Optional.ofNullable(meta.getCorrId()).orElse(causeId);
        executor.submit(() -> dispatcher.store(causeId, corrId, emitted));
        return meta.getStored();
    }

}

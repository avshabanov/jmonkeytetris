package io.shabanov.jmonkeytetris.util;

import lombok.experimental.UtilityClass;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple utility that offers single-threaded publish-subscribe functionality.
 */
@ParametersAreNonnullByDefault
@UtilityClass public class PubSub {

    public interface Event {}

    public interface Subscriber<TEvent extends Event> {
        void receive(TEvent event);
    }

    public interface Subscription {
        void unsubscribe();
    }

    public interface Manager {
        <TEvent extends Event> Subscription subscribe(Class<TEvent> eventType, Subscriber<TEvent> subscriber);
        void publish(Event event);
    }

    public static Manager defaultManager() {
        return new DefaultManager();
    }

    public static Manager noOpManager() {
        return NoOpManager.INSTANCE;
    }

    private static final class DefaultManager implements Manager {
        private final Map<Class<? extends Event>, List<Subscriber<?>>> subscribersMap = new HashMap<>();

        @Override
        public <TEvent extends Event> Subscription subscribe(Class<TEvent> eventType, Subscriber<TEvent> subscriber) {
            final List<Subscriber<?>> subscribers = subscribersMap.computeIfAbsent(eventType, (k) -> new ArrayList<>());
            if (subscribers.contains(subscriber)) {
                throw new IllegalStateException("Double subscription attempt for eventType=" + eventType +
                        ", subscriber=" + subscriber);
            }
            subscribers.add(subscriber);
            return () -> subscribers.remove(subscriber);
        }

        @Override
        public void publish(Event event) {
            final List<Subscriber<?>> subscribers = subscribersMap.get(event.getClass());
            if (subscribers == null) {
                return;
            }
            for (final Subscriber<?> subscriber : subscribers) {
                @SuppressWarnings("unchecked") final Subscriber<Event> eventSubscriber = (Subscriber<Event>) subscriber;
                eventSubscriber.receive(event);
            }
        }
    }

    private static final class NoOpManager implements Manager {
        static final Manager INSTANCE = new NoOpManager();

        @Override
        public <TEvent extends Event> Subscription subscribe(Class<TEvent> eventType, Subscriber<TEvent> subscriber) {
            return () -> {}; // do nothing
        }

        @Override
        public void publish(Event event) {
            // do nothing
        }
    }
}

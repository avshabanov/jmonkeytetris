package io.shabanov.jmonkeytetris.util;

import io.shabanov.jmonkeytetris.util.PubSub;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PubSubTest {

    private PubSub.Manager manager;

    @BeforeEach
    void init() {
        manager = PubSub.defaultManager();
    }

    @Test
    void shouldSendEmptyMessage() {
        manager.publish(new Event1(1));
    }

    @Test
    void shouldSubscribeAndReceiveEvent() {
        // Given:
        final List<Integer> eventSum1 = new ArrayList<>();
        final PubSub.Subscriber<Event1> subscriber1 = (e) -> {
            eventSum1.add(e.foo);
        };

        final List<Integer> eventSum2 = new ArrayList<>();
        final PubSub.Subscriber<Event2> subscriber2 = (e) -> {
            eventSum2.add(e.bar);
        };

        manager.subscribe(Event1.class, subscriber1);
        manager.subscribe(Event2.class, subscriber2);

        // When:
        manager.publish(new Event1(1));
        manager.publish(new Event2(10));

        // Then:
        assertEquals(List.of(1), eventSum1);
        assertEquals(List.of(10), eventSum2);
    }

    @Test
    void shouldUnsubscribe() {
        // Given:
        final List<Integer> eventSum1 = new ArrayList<>();
        final PubSub.Subscriber<Event1> subscriber1 = (e) -> {
            eventSum1.add(e.foo);
        };

        // When:
        final PubSub.Subscription subscription = manager.subscribe(Event1.class, subscriber1);
        subscription.unsubscribe();

        // Then:
        manager.publish(new Event1(1));
        assertEquals(List.of(), eventSum1);
    }

    @Value private static class Event1 implements PubSub.Event {
        int foo;
    }

    @Value private static class Event2 implements PubSub.Event {
        int bar;
    }
}

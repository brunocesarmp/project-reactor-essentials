package dev.brunocesar.reactive.test;


import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;
import java.util.concurrent.Flow;

/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * 1. Publisher sends all the objects requested
 * 2. Publisher sends all the objects ot has. (onComplete) subscriber and subscription will be canceled
 * 3. There is an error. (onError) -> subscriber and subscription will be canceled
 */
public class MonoTest {

    private final static Logger LOG = LoggerFactory.getLogger(MonoTest.class);

    @Test
    public void monoSubscriber() {
        String name = "Bruno Cesar";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();

        StepVerifier.create(mono)
                .expectNext("Bruno Cesar")
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Bruno Cesar";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> LOG.info("Value {}", s));

        StepVerifier.create(mono)
                .expectNext("Bruno Cesar")
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Bruno Cesar";
        Mono<String> mono = Mono.just(name)
                .map(s -> {
                    throw new RuntimeException("Testing mono with error.");
                });

        mono.subscribe(s -> LOG.info("Value {}", s), s -> LOG.error("Something bad happened"));
        mono.subscribe(s -> LOG.info("Value {}", s), Throwable::printStackTrace);

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "Bruno Cesar";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase(Locale.ROOT));

        mono.subscribe(s -> LOG.info("Value {}", s),
                Throwable::printStackTrace,
                () -> LOG.info("FINISHED!"));

        StepVerifier.create(mono)
                .expectNext("BRUNO CESAR")
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "Bruno Cesar";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase(Locale.ROOT));

        mono.subscribe(s -> LOG.info("Value {}", s),
                Throwable::printStackTrace,
                () -> LOG.info("FINISHED!"),
                Subscription::cancel);

        StepVerifier.create(mono)
                .expectNext("BRUNO CESAR")
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethod() {
        String name = "Bruno Cesar";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(s -> s.toUpperCase(Locale.ROOT))
                .doOnSubscribe(subscription -> LOG.info("Subscribed"))
                .doOnRequest(longNumber -> LOG.info("Request Received, stating doing something..."))
                .doOnNext(s -> LOG.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s -> LOG.info("doOnSuccess executed"));

        mono.subscribe(s -> LOG.info("Value {}", s),
                Throwable::printStackTrace,
                () -> LOG.info("FINISHED!"));

        StepVerifier.create(mono)
                .expectNext("BRUNO CESAR")
                .verifyComplete();
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException())
                .doOnError(e -> MonoTest.LOG.error("Error message: {}", e.getMessage()))
                .doOnNext(s -> LOG.info("Executing this doOnNext"))
                .log();

        StepVerifier.create(error)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "Bruno Cesar";
        Mono<Object> error = Mono.error(new IllegalArgumentException())
                .onErrorResume(s ->{
                    LOG.info("inside on Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.LOG.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "Bruno Cesar";
        Mono<Object> error = Mono.error(new IllegalArgumentException())
                .onErrorReturn("EMPTY")
                .onErrorResume(s ->{
                    LOG.info("inside on Error Resume");
                    return Mono.just(name);
                })
                .doOnError(e -> MonoTest.LOG.error("Error message: {}", e.getMessage()))
                .log();

        StepVerifier.create(error)
                .expectNext("EMPTY")
                .verifyComplete();
    }

}

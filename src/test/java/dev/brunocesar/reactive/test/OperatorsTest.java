package dev.brunocesar.reactive.test;

import dev.brunocesar.reactive.test.model.Anime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class OperatorsTest {

    private final static Logger LOG = LoggerFactory.getLogger(OperatorsTest.class);

    @Test
    public void subscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(i -> {
                    LOG.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    LOG.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(i -> {
                    LOG.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    LOG.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void multipleSubscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    LOG.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    LOG.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void multiplePublishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .publishOn(Schedulers.single())
                .map(i -> {
                    LOG.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    LOG.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishAndSubscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .publishOn(Schedulers.single())
                .map(i -> {
                    LOG.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    LOG.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void subscribeAndPublishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .subscribeOn(Schedulers.single())
                .map(i -> {
                    LOG.info("Map 1 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    LOG.info("Map 2 - Number {} on Thread {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void subscribeOnIO() throws InterruptedException {
        Mono<List<String>> list = Mono.fromCallable(() -> Files.readAllLines(Path.of("text-file")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(list)
                .expectSubscription()
                .thenConsumeWhile(l -> {
                    Assertions.assertFalse(l.isEmpty());
                    LOG.info("Size {}", l.size());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void switchIfEmptyOperator() {
        Flux<Object> flux = Flux.empty()
                .switchIfEmpty(Flux.just("Not empty anymore"))
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("Not empty anymore")
                .expectComplete()
                .verify();
    }

    @Test
    public void deferOperator() throws InterruptedException {
        Mono<Long> just = Mono.just(System.currentTimeMillis());

        just.subscribe(l -> LOG.info("time {}", l));
        Thread.sleep(100);
        just.subscribe(l -> LOG.info("time {}", l));
        Thread.sleep(100);
        just.subscribe(l -> LOG.info("time {}", l));
        Thread.sleep(100);

        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

        defer.subscribe(l -> LOG.info("time {}", l));
        Thread.sleep(100);
        defer.subscribe(l -> LOG.info("time {}", l));
        Thread.sleep(100);
        defer.subscribe(l -> LOG.info("time {}", l));
        Thread.sleep(100);

        AtomicLong atomicLong = new AtomicLong();
        defer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get() > 0);

    }

    @Test
    public void concatOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concat(flux1, flux2).log();

        StepVerifier
                .create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatWithOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = flux1.concatWith(flux2).log();

        StepVerifier
                .create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void combineLatestOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> combineLast = Flux.combineLatest(flux1, flux2,
                (s1, s2) -> s1.toUpperCase(Locale.ROOT) + s2.toUpperCase(Locale.ROOT))
                .log();

        StepVerifier
                .create(combineLast)
                .expectSubscription()
                .expectNext("BC", "BD")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeOperator() {
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.merge(flux1, flux2).log();

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWithOperator() {
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = flux1.mergeWith(flux2)
                .delayElements(Duration.ofMillis(200))
                .log();

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("c", "d", "a", "b")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeSequentialOperator() {
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.mergeSequential(flux1, flux2, flux1)
                .delayElements(Duration.ofMillis(200))
                .log();

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("a", "b", "c", "d", "a", "b")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatOperatorError() {
        Flux<String> flux1 = Flux.just("a", "b")
                .map(s -> {
                    if (s.equals("b")) {
                        throw new IllegalArgumentException();
                    }
                    return s;
                });

        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concatDelayError(flux1, flux2).log();

        StepVerifier
                .create(concatFlux)
                .expectSubscription()
                .expectNext("a", "c", "d")
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void mergeDelayErrorOperator() {
        Flux<String> flux1 = Flux.just("a", "b")
                .map(s -> {
                    if (s.equals("b")) {
                        throw new IllegalArgumentException();
                    }
                    return s;
                }).doOnError(t -> LOG.error("We could do something with this"));

        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> mergeFlux = Flux.mergeDelayError(1, flux1, flux2, flux1)
                .log();

        StepVerifier
                .create(mergeFlux)
                .expectSubscription()
                .expectNext("a", "c", "d", "a")
                .expectError()
                .verify();
    }

    @Test
    public void flatMapOperator() {
        Flux<String> flux = Flux.just("a", "b");

        Flux<String> flatFlux = flux.map(String::toUpperCase)
                .flatMap(this::findByName)
                .log();

        StepVerifier
                .create(flatFlux)
                .expectSubscription()
                .expectNext("nameB1", "nameB2", "nameA1", "nameA2")
                .verifyComplete();

    }

    @Test
    public void flatMapSequentialOperator() {
        Flux<String> flux = Flux.just("a", "b");

        Flux<String> flatFlux = flux.map(String::toUpperCase)
                .flatMapSequential(this::findByName)
                .log();

        StepVerifier
                .create(flatFlux)
                .expectSubscription()
                .expectNext("nameA1", "nameA2", "nameB1", "nameB2")
                .verifyComplete();

    }

    private Flux<String> findByName(String name) {
        return name.equals("A") ? Flux.just("nameA1", "nameA2").delayElements(Duration.ofMillis(100)) : Flux.just("nameB1", "nameB2");
    }


    @Test
    public void zipOperator() {
        Flux<String> titleFlux = Flux.just("Grand Blue", "Baki");
        Flux<String> studioFlux = Flux.just("Zero-G", "TMS Entertainment");
        Flux<Integer> episodesFlux = Flux.just(12, 24);

        Flux<Anime> animeFlux = Flux.zip(titleFlux, studioFlux, episodesFlux)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(), tuple.getT2(), tuple.getT3())));

        StepVerifier
                .create(animeFlux)
                .expectSubscription()
                .expectNext(
                        new Anime("Grand Blue", "Zero-G", 12),
                        new Anime("Baki", "TMS Entertainment", 24))
                .verifyComplete();
    }

    @Test
    public void zipWithOperator() {
        Flux<String> titleFlux = Flux.just("Grand Blue", "Baki");
        Flux<Integer> episodesFlux = Flux.just(12, 24);

        Flux<Anime> animeFlux = titleFlux.zipWith(episodesFlux)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(), null, tuple.getT2())));

        StepVerifier
                .create(animeFlux)
                .expectSubscription()
                .expectNext(
                        new Anime("Grand Blue", null, 12),
                        new Anime("Baki", null, 24))
                .verifyComplete();
    }

}

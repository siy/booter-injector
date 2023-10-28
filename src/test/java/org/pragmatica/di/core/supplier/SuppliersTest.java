/*
 * Copyright (c) 2017-2023 Sergiy Yevtushenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pragmatica.di.core.supplier;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.pragmatica.di.Injector;
import org.pragmatica.di.core.exception.InjectorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.pragmatica.di.core.supplier.Suppliers.*;

public class SuppliersTest {
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int NUM_ITERATIONS = NUM_THREADS * 100;

    //TODO: rewrite using JMH
    @Test
    public void measurePerformance() throws Exception {
        var counter = new AtomicInteger();

        measure(lazy(counter::incrementAndGet), "(lambda lazy)");
        assertEquals(counter.get(), 1);
    }

    @Test
    public void shouldCallInitOnlyOnce() throws Exception {
        var counter = new AtomicInteger();

        checkInstantiatedOnce(lazy(counter::incrementAndGet));
        assertEquals(counter.get(), 1);
    }

    @Test
    public void shouldThrowExceptionIfNullIsPassedToLazy() throws Exception {
        assertThrows(InjectorException.class, () -> lazy(null));
    }

    @Test
    public void shouldThrowExceptionIfNullIsPassedToFactoryLazy() throws Exception {
        assertThrows(InjectorException.class, () -> factoryLazy(null));
    }

    @Test
    public void shouldThrowExceptionIfNullIsPassedToSingleton() throws Exception {
        assertThrows(InjectorException.class, () -> singleton(null, false));
    }

    @Test
    public void shouldThrowExceptionIfNullIsPassedToEnhancing1() throws Exception {
        assertThrows(InjectorException.class, () -> enhancing(null, () -> () -> 1));
    }

    @Test
    public void shouldThrowExceptionIfNullIsPassedToEnhancing() throws Exception {
        assertThrows(InjectorException.class, () -> enhancing(() -> 1, null));
    }

    @Test
    public void shouldCreateLazySingleton() {
        var counter = new AtomicInteger();

        var supplier = singleton(counter::incrementAndGet, false);
        assertEquals(counter.get(), 0);

        var value1 = supplier.get();
        assertEquals(value1, 1);
        assertEquals(counter.get(), 1);

        var value2 = supplier.get();
        assertEquals(value2, 1);
        assertEquals(counter.get(), 1);

        assertEquals(value1, value2);
    }

    @Test
    public void shouldProgressivelyEnhance() {
        var supplier = enhancing(() -> 1, () -> () -> 2);

        assertEquals(supplier.get(), 1);
        assertEquals(supplier.get(), 1);
        assertEquals(supplier.get(), 1);
        assertEquals(supplier.get(), 2);
    }

    private void measure(Supplier<Integer> supplier, String type) throws Exception {
        try (var pool = Executors.newFixedThreadPool(NUM_THREADS)) {
            Callable<Integer> fn = supplier::get;
            var callables = IntStream.range(0, NUM_THREADS)
                                     .mapToObj(n -> fn)
                                     .toList();
            var start = System.nanoTime();

            for (var i = 0; i < NUM_ITERATIONS; i++) {
                for (var future : pool.invokeAll(callables)) {
                    assertEquals(future.get(), 1);
                }
            }

            System.out.printf("Time %s : %.2fms\n", type, (System.nanoTime() - start) / 1000000.0);
        }
    }

    private void checkInstantiatedOnce(Supplier<Integer> supplier) throws Exception {
        try (var pool = Executors.newFixedThreadPool(NUM_THREADS)) {
            var barrier = new CyclicBarrier(NUM_THREADS);
            Callable<Integer> fn = () -> { barrier.await(); return supplier.get();};

            var callables = IntStream.range(0, NUM_THREADS)
                                     .mapToObj(n -> fn)
                                     .collect(Collectors.toList());

            for (var future : pool.invokeAll(callables)) {
                assertEquals(future.get(), 1);
            }
        }
    }
}

package io.booter.injector.core.supplier;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class SuppliersTest {
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int NUM_ITERATIONS = NUM_THREADS * 100;

    @Test
    public void measurePerformance() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        measure(Suppliers.lazy(counter::incrementAndGet), "(lambda lazy)");
        assertEquals(1, counter.get());
    }

    @Test
    public void shouldCallInitOnlyOnce() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        checkInstantiatedOnce(Suppliers.lazy(counter::incrementAndGet));
        assertEquals(1, counter.get());
    }

    private void measure(Supplier<Integer> supplier, String type) throws InterruptedException, java.util.concurrent.ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        List<Callable<Integer>> callables = IntStream.range(0, NUM_THREADS)
                                                     .mapToObj(n -> (Callable<Integer>) () -> supplier.get())
                                                     .collect(Collectors.toList());

        long start = System.nanoTime();
        for(int i = 0; i < NUM_ITERATIONS; i++) {
            for (Future<Integer> future : pool.invokeAll(callables)) {
                assertEquals(Integer.valueOf(1), future.get());
            }
        }
        System.out.printf("Time %s : %.2fms\n",  type, (System.nanoTime() - start)/1000000.0);
    }

    private void checkInstantiatedOnce(Supplier<Integer> supplier) throws InterruptedException, java.util.concurrent.ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS);
        List<Callable<Integer>> callables = IntStream.range(0, NUM_THREADS)
                                                     .mapToObj(n -> (Callable<Integer>) () -> {
                                                         barrier.await();
                                                         return supplier.get();
                                                     })
                                                     .collect(Collectors.toList());

        for(Future<Integer> future : pool.invokeAll(callables)) {
            assertEquals(Integer.valueOf(1), future.get());
        }
    }
}

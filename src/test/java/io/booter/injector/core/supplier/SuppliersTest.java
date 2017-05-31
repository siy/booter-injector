package io.booter.injector.core.supplier;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class SuppliersTest {
    private static final int NUM_THREADS = 32;
    private static final int NUM_ITERATIONS = 50_000;

    @Test
    public void measurePerformance2() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        measure(Suppliers.doubleCheckedLazy(counter::incrementAndGet), "(double checked lazy)");
    }

    @Test
    public void measurePerformance3() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        measure(Suppliers.markableReferenceLazy(counter::incrementAndGet), "(markable reference lazy)");
    }

    @Test
    public void measurePerformance1() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        measure(Suppliers.lambdaLazy(counter::incrementAndGet), "(lambda lazy)");
    }

    @Test
    public void shouldCallInitOnlyOnce1() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        checkInstantiatedOnce(Suppliers.lambdaLazy(counter::incrementAndGet));
        assertEquals(1, counter.get());
    }

    @Test
    public void shouldCallInitOnlyOnce2() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        checkInstantiatedOnce(Suppliers.doubleCheckedLazy(counter::incrementAndGet));
        assertEquals(1, counter.get());
    }

    @Test
    public void shouldCallInitOnlyOnce3() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        checkInstantiatedOnce(Suppliers.markableReferenceLazy(counter::incrementAndGet));
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
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS * 2);

        List<Callable<Integer>> callables = IntStream.range(0, NUM_THREADS * 2)
                                                     .mapToObj(n -> (Callable<Integer>) () -> supplier.get())
                                                     .collect(Collectors.toList());

        for(Future<Integer> future : pool.invokeAll(callables)) {
            assertEquals(Integer.valueOf(1), future.get());
        }
    }
}

package io.booter.injector.core.supplier;

import io.booter.injector.core.beans.ClassWithDefaultConstructor;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

//TODO: tests for parameter validation
public class SuppliersTest {
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int NUM_ITERATIONS = NUM_THREADS * 100;

    @Test
    public void measurePerformance() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        measure(Suppliers.lazy(counter::incrementAndGet), "(lambda lazy)");
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void shouldCallInitOnlyOnce() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        checkInstantiatedOnce(Suppliers.lazy(counter::incrementAndGet));
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void shouldCreateLazySingleton() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        Supplier<Integer> supplier = Suppliers.singleton(() -> counter.incrementAndGet(), false);
        assertThat(counter.get()).isEqualTo(0);

        Integer value1 = supplier.get();
        assertThat(value1).isEqualTo(1);
        assertThat(counter.get()).isEqualTo(1);

        Integer value2 = supplier.get();
        assertThat(value2).isEqualTo(1);
        assertThat(counter.get()).isEqualTo(1);

        assertThat(value1).isSameAs(value2);
    }

    @Test
    public void shouldProgressivelyEnhance() throws Exception {
        Supplier<Integer> supplier = Suppliers.enhancing(() -> 1, () -> () -> 2);

        assertThat(supplier.get()).isEqualTo(1);
        assertThat(supplier.get()).isEqualTo(1);
        assertThat(supplier.get()).isEqualTo(1);
        assertThat(supplier.get()).isEqualTo(2);
    }

    @Test
    public void shouldCreateMethodInstantiator() throws Exception {
        Method method = getClass().getDeclaredMethod("method1", int.class);
        AtomicInteger counter = new AtomicInteger();
        Supplier<?>[] parameters = new Supplier[2];
        parameters[0] = () -> this;
        parameters[1] = () -> counter.incrementAndGet();

        Supplier<String> supplier = Suppliers.instantiator(method, parameters);

        assertThat(supplier.get()).isEqualTo("1");
        assertThat(supplier.get()).isEqualTo("2");
    }

    @Test
    public void shouldCreateFastMethodConstructor() throws Exception {
        Method method = getClass().getDeclaredMethod("method1", int.class);
        AtomicInteger counter = new AtomicInteger();
        Supplier<?>[] parameters = new Supplier[2];
        parameters[0] = () -> this;
        parameters[1] = () -> counter.incrementAndGet();

        Supplier<String> supplier = Suppliers.fastMethodConstructor(method, parameters);

        assertThat(supplier.get()).isEqualTo("1");
        assertThat(supplier.get()).isEqualTo("2");
    }

    @Test
    public void shouldCreateFastConstructor() throws Exception {
        Constructor<ClassWithDefaultConstructor> constructor = ClassWithDefaultConstructor.class.getDeclaredConstructor();
        Supplier<?>[] parameters = new Supplier[0];

        Supplier<ClassWithDefaultConstructor> supplier = Suppliers.fastConstructor(constructor, parameters);

        assertThat(supplier).isNotNull();
        assertThat(supplier.get()).isInstanceOf(ClassWithDefaultConstructor.class);
    }

    @Test
    public void shouldCreateConstructor() throws Exception {
        Constructor<ClassWithDefaultConstructor> constructor = ClassWithDefaultConstructor.class.getDeclaredConstructor();
        Supplier<?>[] parameters = new Supplier[0];

        Supplier<ClassWithDefaultConstructor> supplier = Suppliers.constructor(constructor, parameters);

        assertThat(supplier).isNotNull();
        assertThat(supplier.get()).isInstanceOf(ClassWithDefaultConstructor.class);
    }

    public String method1(int val) {
        return Integer.toString(val);
    }

    private void measure(Supplier<Integer> supplier, String type) throws InterruptedException, java.util.concurrent.ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

        List<Callable<Integer>> callables = IntStream.range(0, NUM_THREADS)
                                                     .mapToObj(n -> (Callable<Integer>) () -> supplier.get())
                                                     .collect(Collectors.toList());

        long start = System.nanoTime();
        for(int i = 0; i < NUM_ITERATIONS; i++) {
            for (Future<Integer> future : pool.invokeAll(callables)) {
                assertThat(future.get()).isEqualTo(1);
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
            assertThat(future.get()).isEqualTo(1);
        }
    }
}

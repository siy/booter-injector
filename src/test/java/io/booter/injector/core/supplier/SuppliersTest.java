package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.booter.injector.core.beans.ClassWith1ParameterConstructor;
import io.booter.injector.core.beans.ClassWith2ParametersConstructor;
import io.booter.injector.core.beans.ClassWithDefaultConstructor;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToLazy() throws Exception {
        Suppliers.lazy(null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToFactoryLazy() throws Exception {
        Suppliers.factoryLazy(null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToSingleton() throws Exception {
        Suppliers.singleton(null, false);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToEnhancing1() throws Exception {
        Suppliers.enhancing(null, () -> () -> 1);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToEnhancing() throws Exception {
        Suppliers.enhancing(() -> 1, null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToInstantiator1() throws Exception {
        Suppliers.methodSupplier(null, new Supplier[0]);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToInstantiator2() throws Exception {
        Method method = getClass().getDeclaredMethod("method1", int.class);
        Suppliers.methodSupplier(method, (Supplier<?>[]) null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNotEnoughParametersPassedToInstantiator() throws Exception {
        Method method = getClass().getDeclaredMethod("method1", int.class);
        Supplier<?>[] parameters = new Supplier[1];
        parameters[0] = () -> this;

        Suppliers.methodSupplier(method, parameters);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToConstructor1() throws Exception {
        Suppliers.constructorSupplier(null, new Supplier[0]);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToConstructor2() throws Exception {
        Constructor<ClassWithDefaultConstructor> constructor = ClassWithDefaultConstructor.class.getDeclaredConstructor();
        Suppliers.constructorSupplier(constructor, (Supplier<?>[]) null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNotEnoughParametersPassedToConstructor() throws Exception {
        Constructor<ClassWith1ParameterConstructor> constructor = ClassWith1ParameterConstructor.class.getDeclaredConstructor(Long.class);
        Suppliers.constructorSupplier(constructor, new Supplier[0]);
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

        Supplier<String> supplier = Suppliers.methodSupplier(method, parameters);

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
        Constructor<ClassWith2ParametersConstructor> constructor = (Constructor<ClassWith2ParametersConstructor>) ClassWith2ParametersConstructor.class.getDeclaredConstructors()[0];
        Supplier<?>[] parameters = new Supplier[] { () -> Long.valueOf(592L), () -> "aBc"};

        Supplier<ClassWith2ParametersConstructor> supplier = Suppliers.constructorSupplier(constructor, parameters);

        assertThat(supplier).isNotNull();
        assertThat(supplier.get()).isInstanceOf(ClassWith2ParametersConstructor.class);
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

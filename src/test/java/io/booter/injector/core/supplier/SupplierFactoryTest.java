package io.booter.injector.core.supplier;

import io.booter.injector.core.beans.*;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.booter.injector.core.supplier.SupplierFactory.*;
import static org.assertj.core.api.Assertions.*;

public class SupplierFactoryTest {
    private static final List<Supplier<?>> EMPTY = Collections.emptyList();

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsParameterToCreate1() throws Exception {
        createInstanceSupplier(null, EMPTY);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsParameterToCreate2() throws Exception {
        createInstanceSupplier(constructor(ClassWithDefaultConstructor.class), null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNotEnoughParametersArePasseToCreate() throws Exception {
        createInstanceSupplier(constructor(ClassWith1ParameterConstructor.class), EMPTY);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsParameterToCreateSingleton1() throws Exception {
        createSingletonSupplier(null, EMPTY, false);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsParameterToCreateSingleton2() throws Exception {
        createSingletonSupplier(constructor(ClassWithDefaultConstructor.class),null, false);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNotEnoughParametersArePasseToCreateSingleton() throws Exception {
        createSingletonSupplier(constructor(ClassWith1ParameterConstructor.class), EMPTY, true);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToCreatMethod1() throws Exception {
        createMethodSupplier(null, Collections.emptyList());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullIsPassedToInstantiator2() throws Exception {
        Method method = getClass().getDeclaredMethod("method1", int.class);
        createMethodSupplier(method, null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNotEnoughParametersPassedToInstantiator() throws Exception {
        Method method = getClass().getDeclaredMethod("method1", int.class);
        createMethodSupplier(method, Collections.singletonList(() -> this));
    }

    @Test
    public void shouldCreateFactorySupplier() throws Exception {
        Supplier<ClassWithDefaultConstructor> supplier = createInstanceSupplier(constructor(ClassWithDefaultConstructor.class), EMPTY);
        assertThat(supplier).isNotNull();

        ClassWithDefaultConstructor instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(ClassWithDefaultConstructor.class);

        ClassWithDefaultConstructor instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(ClassWithDefaultConstructor.class);
        assertThat(instance1).isNotSameAs(instance2);
    }

    @Test
    public void shouldCreateLazySingletonSupplierFromClassMarkedAsSingleton() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        Consumer<LazySingletonClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();
        Supplier<LazySingletonClassWithNotifyingConstructor> supplier = createInstanceSupplier(constructor(LazySingletonClassWithNotifyingConstructor.class),
                                                                               Collections.singletonList(() -> consumer));
        assertThat(supplier).isNotNull();

        LazySingletonClassWithNotifyingConstructor instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(LazySingletonClassWithNotifyingConstructor.class);

        LazySingletonClassWithNotifyingConstructor instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(LazySingletonClassWithNotifyingConstructor.class);
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void shouldCreateEagerSingletonSupplierFromClassMarkedAsSingleton() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        Consumer<EagerSingletonClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();

        assertThat(counter.get()).isEqualTo(0);
        Supplier<EagerSingletonClassWithNotifyingConstructor> supplier = createInstanceSupplier(constructor(EagerSingletonClassWithNotifyingConstructor.class),
                Collections.singletonList(() -> consumer));
        assertThat(supplier).isNotNull();
        assertThat(counter.get()).isEqualTo(1);

        EagerSingletonClassWithNotifyingConstructor instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(EagerSingletonClassWithNotifyingConstructor.class);
        assertThat(counter.get()).isEqualTo(1);

        EagerSingletonClassWithNotifyingConstructor instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(EagerSingletonClassWithNotifyingConstructor.class);
        assertThat(instance1).isSameAs(instance2);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void shouldCreateLazySingletonSupplierForRegularClass() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        Consumer<ClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();

        Supplier<ClassWithNotifyingConstructor> supplier = createSingletonSupplier(constructor(ClassWithNotifyingConstructor.class),
                Collections.singletonList(() -> consumer), false);

        assertThat(supplier).isNotNull();
        assertThat(counter.get()).isEqualTo(0);

        ClassWithNotifyingConstructor instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(ClassWithNotifyingConstructor.class);
        assertThat(counter.get()).isEqualTo(1);

        ClassWithNotifyingConstructor instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(ClassWithNotifyingConstructor.class);
        assertThat(instance1).isSameAs(instance2);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void shouldCreateEagerSingletonSupplierForRegularClass() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        Consumer<ClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();

        Supplier<ClassWithNotifyingConstructor> supplier = createSingletonSupplier(constructor(ClassWithNotifyingConstructor.class),
                Collections.singletonList(() -> consumer), true);

        assertThat(supplier).isNotNull();
        assertThat(counter.get()).isEqualTo(1);

        ClassWithNotifyingConstructor instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(ClassWithNotifyingConstructor.class);
        assertThat(counter.get()).isEqualTo(1);

        ClassWithNotifyingConstructor instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(ClassWithNotifyingConstructor.class);
        assertThat(instance1).isSameAs(instance2);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void shouldCreateSupplierForClassWithPostConstructAndCallPostConstruct() throws Exception {
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = twinConsumer(counter1, counter2);

        Supplier<ClassWithPostConstruct> supplier = createInstanceSupplier(constructor(ClassWithPostConstruct.class),
                Collections.singletonList(() -> consumer));

        assertThat(supplier).isNotNull();
        assertThat(counter1.get()).isEqualTo(0);
        assertThat(counter2.get()).isEqualTo(0);

        ClassWithPostConstruct instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(ClassWithPostConstruct.class);
        assertThat(counter1.get()).isEqualTo(1);
        assertThat(counter2.get()).isEqualTo(1);

        ClassWithPostConstruct instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(ClassWithPostConstruct.class);
        assertThat(instance1).isNotSameAs(instance2);
        assertThat(counter1.get()).isEqualTo(2);
        assertThat(counter2.get()).isEqualTo(2);
    }

    private Consumer<Integer> twinConsumer(AtomicInteger counter1, AtomicInteger counter2) {
        return (val) -> {
                if(val == 1) {
                    counter1.incrementAndGet();
                } else if (val == 2) {
                    counter2.incrementAndGet();
                }
            };
    }

    @Test
    public void shouldCreateLazySingletonSupplierForClassWithPostConstructAndCallPostConstruct() throws Exception {
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = twinConsumer(counter1, counter2);

        Supplier<ClassWithPostConstruct> supplier = createSingletonSupplier(constructor(ClassWithPostConstruct.class),
                Collections.singletonList(() -> consumer), false);

        assertThat(supplier).isNotNull();
        assertThat(counter1.get()).isEqualTo(0);
        assertThat(counter2.get()).isEqualTo(0);

        ClassWithPostConstruct instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(ClassWithPostConstruct.class);
        assertThat(counter1.get()).isEqualTo(1);
        assertThat(counter2.get()).isEqualTo(1);

        ClassWithPostConstruct instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(ClassWithPostConstruct.class);
        assertThat(instance1).isSameAs(instance2);
        assertThat(counter1.get()).isEqualTo(1);
        assertThat(counter2.get()).isEqualTo(1);
    }

    @Test
    public void shouldCreateEagerSingletonSupplierForClassWithPostConstructAndCallPostConstruct() throws Exception {
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = twinConsumer(counter1, counter2);

        Supplier<ClassWithPostConstruct> supplier = createSingletonSupplier(constructor(ClassWithPostConstruct.class),
                Collections.singletonList(() -> consumer), true);

        assertThat(supplier).isNotNull();
        assertThat(counter1.get()).isEqualTo(1);
        assertThat(counter2.get()).isEqualTo(1);

        ClassWithPostConstruct instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(ClassWithPostConstruct.class);
        assertThat(counter1.get()).isEqualTo(1);
        assertThat(counter2.get()).isEqualTo(1);

        ClassWithPostConstruct instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(ClassWithPostConstruct.class);
        assertThat(instance1).isSameAs(instance2);
        assertThat(counter1.get()).isEqualTo(1);
        assertThat(counter2.get()).isEqualTo(1);
    }

    @Test(expected = InjectorException.class)
    public void shouldReportExceptionsThrownByPostConstruct() throws Exception {
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = twinConsumer(counter1, counter2);

        Supplier<ClassWithThrowingPostConstruct> supplier = createSingletonSupplier(constructor(ClassWithThrowingPostConstruct.class),
                Collections.singletonList(() -> consumer), false);

        assertThat(supplier).isNotNull();
        assertThat(counter1.get()).isEqualTo(0);
        assertThat(counter2.get()).isEqualTo(0);

        supplier.get();
        fail("No exception is thrown, although expected");
    }

    @Test
    public void shouldCreateSupplierFromMethod() throws Exception {
        Method method = getClass().getDeclaredMethod("method1", int.class);
        AtomicInteger counter = new AtomicInteger();

        Supplier<String> supplier = createMethodSupplier(method, Arrays.asList(() -> this, counter::incrementAndGet));

        assertThat(supplier.get()).isEqualTo("1");
        assertThat(supplier.get()).isEqualTo("2");
    }

    public String method1(int val) {
        return Integer.toString(val);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> constructor(Class<T> clazz) {
        return (Constructor<T>) clazz.getDeclaredConstructors()[0];
    }
}
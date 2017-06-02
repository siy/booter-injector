package io.booter.injector.core.supplier;

import io.booter.injector.core.SupplierFactory;
import io.booter.injector.core.beans.*;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

public class DefaultSupplierFactoryTest {
    private static final Supplier<?>[] EMPTY = new Supplier[0];

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsPatameterToCreate1() throws Exception {
        new DefaultSupplierFactory().create(null, EMPTY);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsPatameterToCreate2() throws Exception {
        new DefaultSupplierFactory().create(constructor(ClassWithDefaultConstructor.class), null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNotEnoughParametersArePasseToCreate() throws Exception {
        new DefaultSupplierFactory().create(constructor(ClassWith1ParameterConstructor.class), EMPTY);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsPatameterToCreateSingleton1() throws Exception {
        new DefaultSupplierFactory().createSingleton(null, EMPTY, false);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNullPassedAsPatameterToCreateSingleton2() throws Exception {
        new DefaultSupplierFactory().createSingleton(constructor(ClassWithDefaultConstructor.class), null, false);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNotEnoughParametersArePasseToCreateSingleton() throws Exception {
        new DefaultSupplierFactory().createSingleton(constructor(ClassWith1ParameterConstructor.class), EMPTY, true);
    }

    @Test
    public void shouldCreateFactorySupplier() throws Exception {
        SupplierFactory factory = new DefaultSupplierFactory();

        Supplier<ClassWithDefaultConstructor> supplier = factory.create(constructor(ClassWithDefaultConstructor.class), EMPTY);
        assertThat(supplier).isNotNull();

        ClassWithDefaultConstructor instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(ClassWithDefaultConstructor.class);

        ClassWithDefaultConstructor instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(ClassWithDefaultConstructor.class);
        assertThat(instance1).isNotSameAs(instance2);
    }

    @Test
    public void shouldCreateLazySingletonSupplierFromClassMarkedAsSingleton() throws Exception {
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter = new AtomicInteger();
        Consumer<LazySingletonClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};

        Supplier<LazySingletonClassWithNotifyingConstructor> supplier = factory.create(constructor(LazySingletonClassWithNotifyingConstructor.class), parameters);
        assertThat(supplier).isNotNull();

        LazySingletonClassWithNotifyingConstructor instance1 = supplier.get();
        assertThat(instance1).isInstanceOf(LazySingletonClassWithNotifyingConstructor.class);

        LazySingletonClassWithNotifyingConstructor instance2 = supplier.get();
        assertThat(instance2).isInstanceOf(LazySingletonClassWithNotifyingConstructor.class);
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void shouldCreateEagerSingletonSupplierFromClassMarkedAsSingleton() throws Exception {
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter = new AtomicInteger();
        Consumer<EagerSingletonClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};


        assertThat(counter.get()).isEqualTo(0);
        Supplier<EagerSingletonClassWithNotifyingConstructor> supplier = factory.create(constructor(EagerSingletonClassWithNotifyingConstructor.class), parameters);
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
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter = new AtomicInteger();
        Consumer<ClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};

        Supplier<ClassWithNotifyingConstructor> supplier = factory.createSingleton(constructor(ClassWithNotifyingConstructor.class), parameters, false);

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
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter = new AtomicInteger();
        Consumer<ClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};

        Supplier<ClassWithNotifyingConstructor> supplier = factory.createSingleton(constructor(ClassWithNotifyingConstructor.class), parameters, true);

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
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = (val) -> {
            if(val == 1) {
                counter1.incrementAndGet();
            } else if (val == 2) {
                counter2.incrementAndGet();
            }
        };
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};

        Supplier<ClassWithPostConstruct> supplier = factory.create(constructor(ClassWithPostConstruct.class), parameters);

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

    @Test
    public void shouldCreateLazySingletonSupplierForClassWithPostConstructAndCallPostConstruct() throws Exception {
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = (val) -> {
            if(val == 1) {
                counter1.incrementAndGet();
            } else if (val == 2) {
                counter2.incrementAndGet();
            }
        };
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};

        Supplier<ClassWithPostConstruct> supplier = factory.createSingleton(constructor(ClassWithPostConstruct.class), parameters, false);

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
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = (val) -> {
            if(val == 1) {
                counter1.incrementAndGet();
            } else if (val == 2) {
                counter2.incrementAndGet();
            }
        };
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};

        Supplier<ClassWithPostConstruct> supplier = factory.createSingleton(constructor(ClassWithPostConstruct.class), parameters, true);

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
        SupplierFactory factory = new DefaultSupplierFactory();
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        Consumer<Integer> consumer = (val) -> {
            if(val == 1) {
                counter1.incrementAndGet();
            } else if (val == 2) {
                counter2.incrementAndGet();
            }
        };
        Supplier<?>[] parameters = new Supplier[] { () -> consumer};

        Supplier<ClassWithThrowingPostConstruct> supplier = factory.createSingleton(constructor(ClassWithThrowingPostConstruct.class), parameters, false);

        assertThat(supplier).isNotNull();
        assertThat(counter1.get()).isEqualTo(0);
        assertThat(counter2.get()).isEqualTo(0);

        supplier.get();
        fail("No exception is thrown, although expected");
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> constructor(Class<T> clazz) {
        return (Constructor<T>) clazz.getDeclaredConstructors()[0];
    }
}
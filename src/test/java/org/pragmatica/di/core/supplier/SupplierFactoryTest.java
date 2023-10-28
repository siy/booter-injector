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

import org.junit.jupiter.api.Test;
import org.pragmatica.di.core.beans.*;
import org.pragmatica.di.core.exception.InjectorException;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.pragmatica.di.core.supplier.SupplierFactory.*;

public class SupplierFactoryTest {
    private static final List<Supplier<?>> EMPTY = List.of();

    @Test
    public void shouldThrowExceptionIfNullPassedAsParameterToCreate1() throws Exception {
        assertThrows(InjectorException.class, () -> createInstanceSupplier(null, EMPTY));
    }

    @Test
    public void shouldThrowExceptionIfNullPassedAsParameterToCreate2() throws Exception {
        assertThrows(InjectorException.class, () -> createInstanceSupplier(constructor(ClassWithDefaultConstructor.class), null));
    }

    @Test
    public void shouldThrowExceptionIfNotEnoughParametersArePasseToCreate() throws Exception {
        assertThrows(InjectorException.class, () -> createInstanceSupplier(constructor(ClassWith1ParameterConstructor.class), EMPTY));
    }

    @Test
    public void shouldThrowExceptionIfNullPassedAsParameterToCreateSingleton1() throws Exception {
        assertThrows(InjectorException.class, () -> createSingletonSupplier(null, EMPTY, false));
    }

    @Test
    public void shouldThrowExceptionIfNullPassedAsParameterToCreateSingleton2() throws Exception {
        assertThrows(InjectorException.class, () -> createSingletonSupplier(constructor(ClassWithDefaultConstructor.class), null, false));
    }

    @Test
    public void shouldThrowExceptionIfNotEnoughParametersArePasseToCreateSingleton() throws Exception {
        assertThrows(InjectorException.class, () -> createSingletonSupplier(constructor(ClassWith1ParameterConstructor.class), EMPTY, true));
    }

    @Test
    public void shouldThrowExceptionIfNullIsPassedToCreatMethod1() {
        assertThrows(InjectorException.class, () -> createMethodSupplier(null, List.of()));
    }

    @Test
    public void shouldThrowExceptionIfNullIsPassedToInstantiator2() throws Exception {
        var method = getClass().getDeclaredMethod("method1", int.class);

        assertThrows(InjectorException.class, () -> createMethodSupplier(method, null));
    }

    @Test
    public void shouldThrowExceptionIfNotEnoughParametersPassedToInstantiator() throws Exception {
        var method = getClass().getDeclaredMethod("method1", int.class);

        assertThrows(InjectorException.class, () -> createMethodSupplier(method, List.of(() -> this)));
    }

    @Test
    public void shouldCreateFactorySupplier() {
        var supplier = createInstanceSupplier(constructor(ClassWithDefaultConstructor.class), EMPTY);

        assertNotNull(supplier);

        var instance1 = supplier.get();
        assertNotNull(instance1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertNotSame(instance1, instance2);
    }

    @Test
    public void shouldCreateLazySingletonSupplierFromClassMarkedAsSingleton() {
        var counter = new AtomicInteger();
        Consumer<LazySingletonClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();
        var supplier = createInstanceSupplier(constructor(LazySingletonClassWithNotifyingConstructor.class), List.of(() -> consumer));
        assertNotNull(supplier);

        var instance1 = supplier.get();
        assertNotNull(instance1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    public void shouldCreateEagerSingletonSupplierFromClassMarkedAsSingleton() {
        var counter = new AtomicInteger();
        Consumer<EagerSingletonClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();

        assertEquals(counter.get(), 0);
        var supplier = createInstanceSupplier(constructor(EagerSingletonClassWithNotifyingConstructor.class),
                                              List.of(() -> consumer));
        assertNotNull(supplier);
        assertEquals(counter.get(), 1);

        var instance1 = supplier.get();
        assertNotNull(instance1);
        assertEquals(counter.get(), 1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertSame(instance1, instance2);
        assertEquals(counter.get(), 1);
    }

    @Test
    public void shouldCreateLazySingletonSupplierForRegularClass() throws Exception {
        var counter = new AtomicInteger();
        Consumer<ClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();

        var supplier = createSingletonSupplier(constructor(ClassWithNotifyingConstructor.class),
                                               List.of(() -> consumer), false);

        assertNotNull(supplier);
        assertEquals(counter.get(), 0);

        var instance1 = supplier.get();
        assertNotNull(instance1);
        assertEquals(counter.get(), 1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertSame(instance1, instance2);
        assertEquals(counter.get(), 1);
    }

    @Test
    public void shouldCreateEagerSingletonSupplierForRegularClass() throws Exception {
        var counter = new AtomicInteger();
        Consumer<ClassWithNotifyingConstructor> consumer = (instance) -> counter.incrementAndGet();

        var supplier = createSingletonSupplier(constructor(ClassWithNotifyingConstructor.class),
                                               List.of(() -> consumer), true);

        assertNotNull(supplier);
        assertEquals(counter.get(), 1);

        var instance1 = supplier.get();
        assertNotNull(instance1);
        assertEquals(counter.get(), 1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertSame(instance1, instance2);
        assertEquals(counter.get(), 1);
    }

    @Test
    public void shouldCreateSupplierForClassWithPostConstructAndCallPostConstruct() throws Exception {
        var counter1 = new AtomicInteger();
        var counter2 = new AtomicInteger();
        var consumer = twinConsumer(counter1, counter2);

        var supplier = createInstanceSupplier(constructor(ClassWithPostConstruct.class),
                                              List.of(() -> consumer));

        assertNotNull(supplier);
        assertEquals(counter1.get(), 0);
        assertEquals(counter2.get(), 0);

        var instance1 = supplier.get();
        assertNotNull(instance1);
        assertEquals(counter1.get(), 1);
        assertEquals(counter2.get(), 1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertNotSame(instance1, instance2);
        assertEquals(counter1.get(), 2);
        assertEquals(counter2.get(), 2);
    }

    private Consumer<Integer> twinConsumer(AtomicInteger counter1, AtomicInteger counter2) {
        return (val) -> {
            if (val == 1) {
                counter1.incrementAndGet();
            } else if (val == 2) {
                counter2.incrementAndGet();
            }
        };
    }

    @Test
    public void shouldCreateLazySingletonSupplierForClassWithPostConstructAndCallPostConstruct() throws Exception {
        var counter1 = new AtomicInteger();
        var counter2 = new AtomicInteger();
        var consumer = twinConsumer(counter1, counter2);

        var supplier = createSingletonSupplier(constructor(ClassWithPostConstruct.class),
                                               List.of(() -> consumer), false);

        assertNotNull(supplier);
        assertEquals(counter1.get(), 0);
        assertEquals(counter2.get(), 0);

        var instance1 = supplier.get();
        assertNotNull(instance1);
        assertEquals(counter1.get(), 1);
        assertEquals(counter2.get(), 1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertSame(instance1, instance2);
        assertEquals(counter1.get(), 1);
        assertEquals(counter2.get(), 1);
    }

    @Test
    public void shouldCreateEagerSingletonSupplierForClassWithPostConstructAndCallPostConstruct() throws Exception {
        var counter1 = new AtomicInteger();
        var counter2 = new AtomicInteger();
        var consumer = twinConsumer(counter1, counter2);

        var supplier = createSingletonSupplier(constructor(ClassWithPostConstruct.class),
                                               List.of(() -> consumer), true);

        assertNotNull(supplier);
        assertEquals(counter1.get(), 1);
        assertEquals(counter2.get(), 1);

        var instance1 = supplier.get();
        assertNotNull(instance1);
        assertEquals(counter1.get(), 1);
        assertEquals(counter2.get(), 1);

        var instance2 = supplier.get();
        assertNotNull(instance2);
        assertSame(instance1, instance2);
        assertEquals(counter1.get(), 1);
        assertEquals(counter2.get(), 1);
    }

    @Test
    public void shouldReportExceptionsThrownByPostConstruct() throws Exception {
        var counter1 = new AtomicInteger();
        var counter2 = new AtomicInteger();
        var consumer = twinConsumer(counter1, counter2);

        var supplier = createSingletonSupplier(constructor(ClassWithThrowingPostConstruct.class),
                                               List.of(() -> consumer), false);

        assertNotNull(supplier);
        assertEquals(counter1.get(), 0);
        assertEquals(counter2.get(), 0);

        assertThrows(InjectorException.class, supplier::get);
    }

    @Test
    public void shouldCreateSupplierFromMethod() throws Exception {
        var method = getClass().getDeclaredMethod("method1", int.class);
        var counter = new AtomicInteger();

        Supplier<String> supplier = createMethodSupplier(method, List.of(() -> this, counter::incrementAndGet));

        assertEquals(supplier.get(), "1");
        assertEquals(supplier.get(), "2");
    }

    public String method1(int val) {
        return Integer.toString(val);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> constructor(Class<T> clazz) {
        return (Constructor<T>) clazz.getDeclaredConstructors()[0];
    }
}
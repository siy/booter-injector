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

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.pragmatica.di.core.beans.*;
import org.pragmatica.di.core.exception.InjectorException;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaFactoryTest {
    private final Supplier<?>[] suppliers = new Supplier<?>[]{
        () -> 1234L,
        () -> "098",
        () -> 10321,
        () -> 2345L,
        () -> "987",
        () -> 10432,
        () -> 3456L,
        () -> "876",
        () -> 10543,
        () -> 3456L,
        () -> "765"
    };

    @Test
    public void shouldCreateBeanWith0Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWithDefaultConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
    }

    @Test
    public void shouldCreateBeanWith1Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith1ParameterConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
    }

    @Test
    public void shouldCreateBeanWith2Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith2ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
    }

    @Test
    public void shouldCreateBeanWith3Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith3ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
    }

    @Test
    public void shouldCreateBeanWith4Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith4ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
        assertEquals(result.get().p3(), 2345L);
    }

    @Test
    public void shouldCreateBeanWith5Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith5ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
        assertEquals(result.get().p3(), 2345L);
        assertEquals(result.get().p4(), "987");
    }

    @Test
    public void shouldCreateBeanWith6Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith6ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
        assertEquals(result.get().p3(), 2345L);
        assertEquals(result.get().p4(), "987");
        assertEquals(result.get().p5(), 10432);
    }

    @Test
    public void shouldCreateBeanWith7Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith7ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
        assertEquals(result.get().p3(), 2345L);
        assertEquals(result.get().p4(), "987");
        assertEquals(result.get().p5(), 10432);
        assertEquals(result.get().p6(), 3456L);
    }

    @Test
    public void shouldCreateBeanWith8Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith8ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
        assertEquals(result.get().p3(), 2345L);
        assertEquals(result.get().p4(), "987");
        assertEquals(result.get().p5(), 10432);
        assertEquals(result.get().p6(), 3456L);
        assertEquals(result.get().p7(), "876");
    }

    @Test
    public void shouldCreateBeanWith9Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith9ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
        assertEquals(result.get().p3(), 2345L);
        assertEquals(result.get().p4(), "987");
        assertEquals(result.get().p5(), 10432);
        assertEquals(result.get().p6(), 3456L);
        assertEquals(result.get().p7(), "876");
        assertEquals(result.get().p8(), 10543);
    }

    @Test
    public void shouldCreateBeanWith10Parameters() throws Exception {
        var result = LambdaFactory.create(constructor(ClassWith10ParametersConstructor.class), suppliers);

        assertNotNull(result);
        assertNotNull(result.get());
        ;
        assertEquals(result.get().p0(), 1234L);
        assertEquals(result.get().p1(), "098");
        assertEquals(result.get().p2(), 10321);
        assertEquals(result.get().p3(), 2345L);
        assertEquals(result.get().p4(), "987");
        assertEquals(result.get().p5(), 10432);
        assertEquals(result.get().p6(), 3456L);
        assertEquals(result.get().p7(), "876");
        assertEquals(result.get().p8(), 10543);
        assertEquals(result.get().p9(), 3456L);
    }

    @Test
    public void shouldFailToCreateSupplierForBeanWith11Parameters() throws Exception {
        assertThrows(InjectorException.class, () -> LambdaFactory.create(constructor(ClassWith11ParametersConstructor.class), suppliers));
    }

    @Test
    public void shouldFailToCreateSupplierWithNotEnoughParameters() throws Exception {
        Supplier<?>[] parameters = new Supplier[1];
        parameters[0] = () -> 12345L;

        assertThrows(InjectorException.class, () -> LambdaFactory.create(constructor(ClassWith2ParametersConstructor.class), parameters));
    }

    @Test
    public void shouldFailToCreateSupplierForNullClass() throws Exception {
        assertThrows(InjectorException.class, () -> LambdaFactory.create((Constructor<?>) null, suppliers));
    }

    @Test
    public void shouldFailToCreateSupplierForNullArguments() throws Exception {
        LambdaFactory.create(constructor(ClassWithDefaultConstructor.class), null);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> constructor(Class<T> clazz) {
        return (Constructor<T>) clazz.getDeclaredConstructors()[0];
    }
}
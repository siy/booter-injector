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

package org.pragmatica.di.core;

import org.junit.jupiter.api.Test;
import org.pragmatica.di.Key;
import org.pragmatica.di.TypeToken;
import org.pragmatica.di.annotations.BindingAnnotation;
import org.pragmatica.di.core.annotation.AnnotationFactory;
import org.pragmatica.di.core.beans.ClassWithDefaultConstructor;
import org.pragmatica.di.core.exception.InjectorException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class KeyTest {
    @Test
    public void shouldCreateKeyForSimpleClass() {
        var key = Key.of(ClassWithDefaultConstructor.class);

        assertNotNull(key);
        assertEquals(key.rawClass(), ClassWithDefaultConstructor.class);
        assertNull(key.annotation());
        assertEquals(key.type().toString(), "class org.pragmatica.di.core.beans.ClassWithDefaultConstructor");
        assertEquals(key.toString(), "{org.pragmatica.di.core.beans.ClassWithDefaultConstructor}");
    }

    @Test
    public void shouldCreateKeyForSimpleClassWithAnnotation() {
        var key = Key.of(ClassWithDefaultConstructor.class, AnnotationFactory.create(TestAnnotation.class));

        assertNotNull(key);
        assertEquals(key.rawClass(), ClassWithDefaultConstructor.class);
        assertTrue(key.annotation() instanceof TestAnnotation);
        assertEquals(key.toString(),
                     "{org.pragmatica.di.core.beans.ClassWithDefaultConstructor @interface org.pragmatica.di.core.KeyTest$TestAnnotation}");
    }

    @Test
    public void shouldIgnoreNonBindingAnnotation() {
        var key = Key.of(ClassWithDefaultConstructor.class,
                         AnnotationFactory.create(TestNonBindingAnnotation.class),
                         AnnotationFactory.create(TestAnnotation.class));

        assertNotNull(key);
        assertEquals(key.rawClass(), ClassWithDefaultConstructor.class);
        assertTrue(key.annotation() instanceof TestAnnotation);
        assertEquals(key.toString(),
                     "{org.pragmatica.di.core.beans.ClassWithDefaultConstructor @interface org.pragmatica.di.core.KeyTest$TestAnnotation}");
    }

    @Test
    public void keyFollowsEqualsContract() {
        var key1 = Key.of(new TypeToken<List<Supplier<String>>>() {});
        var key2 = Key.of(new TypeToken<List<Supplier<String>>>() {});

        assertEquals(key1, key2);
        assertEquals(key2, key1);

        assertEquals(key1, key1);
        assertEquals(key2, key2);

        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(key1, "");
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(key2, "");
    }

    @Test
    public void shouldCreateKeyForTypeToken() {
        var key = Key.of(new TypeToken<List<Supplier<String>>>() {});

        assertNotNull(key);
        assertEquals(key.rawClass(), List.class);
        assertNull(key.annotation());
        assertEquals(key.type().toString(), "java.util.List<java.util.function.Supplier<java.lang.String>>");
        assertEquals(key.toString(), "{java.util.List<java.util.function.Supplier<java.lang.String>>}");
    }

    @Test
    public void shouldCreateKeyForTypeTokenWithAnnotation() {
        var key = Key.of(new TypeToken<List<Supplier<String>>>() {}).with(TestAnnotation.class);

        assertNotNull(key);
        assertEquals(key.rawClass(), List.class);
        assertTrue(key.annotation() instanceof TestAnnotation);
        assertEquals(key.type().toString(), "java.util.List<java.util.function.Supplier<java.lang.String>>");
        assertEquals(key.toString(),
                     "{java.util.List<java.util.function.Supplier<java.lang.String>> @interface org.pragmatica.di.core.KeyTest$TestAnnotation}");
    }

    @Test
    public void shouldDistinguishAnnotatedAndNotAnnotatedKeys() {
        var key1 = Key.of(new TypeToken<List<Supplier<String>>>() {});
        var key2 = key1.with(TestAnnotation.class);

        assertNotNull(key1);
        assertNotNull(key2);
        assertEquals(key1.rawClass(), key2.rawClass());
        assertNotEquals(key1, key2);
    }

    @Test
    public void shouldCreateSameKeyForTypeTokenRegardlessFromTheWay() {
        var key1 = Key.of(new TypeToken<List<Supplier<String>>>() {}).with(TestAnnotation.class);
        var key2 = Key.of(new TypeToken<List<Supplier<String>>>() {}, AnnotationFactory.create(TestAnnotation.class));

        assertNotNull(key1);
        assertNotNull(key2);
        assertEquals(key1, key2);
    }

    @Test
    public void shouldCreateKeyForSimpleParameter() throws Exception {
        var parameter = getClass().getDeclaredMethod("method1", List.class).getParameters()[0];
        var key = Key.of(parameter);

        assertNotNull(key);
        assertEquals(key.toString(), "{java.util.List<java.lang.String>}");
    }

    @Test
    public void shouldCreateKeyForArrayType() {
        var key = Key.of(Supplier[].class);

        assertNotNull(key);
        assertEquals(key.toString(), "{java.util.function.Supplier[]}");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldCreateKeyForGenericArrayType() {
        var key = Key.of(new TypeToken<List<Supplier[]>>() {});

        assertNotNull(key);
        assertEquals(key.toString(), "{java.util.List<java.util.function.Supplier[]>}");
    }

    @Test
    public void shouldCreateKeyForWildcardType() {
        var key = Key.of(new TypeToken<Supplier<?>>() {});

        assertNotNull(key);
        assertEquals(key.toString(), "{java.util.function.Supplier<?>}");
    }

    public String method1(List<String> p0) {
        return (p0 == null || p0.isEmpty()) ? "empty" : p0.get(0);
    }

    @Test
    public void shouldThrowExceptionIfNonBindingAnnotationIsPassedToWith() {
        assertThrows(InjectorException.class, () -> Key.of(String.class).with(TestNonBindingAnnotation.class));
    }

    @Test
    public void shouldThrowExceptionForNullType() {
        assertThrows(InjectorException.class, () -> Key.of((Type) null));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    @Target({ElementType.TYPE, ElementType.PARAMETER})
    public @interface TestAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PARAMETER})
    public @interface TestNonBindingAnnotation {
    }
}
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
import org.pragmatica.di.annotations.ComputationStyle;
import org.pragmatica.di.annotations.Singleton;
import org.pragmatica.di.core.annotation.AnnotationFactory;
import org.pragmatica.di.core.exception.InjectorException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationFactoryTest {
    @Test
    public void shouldCreateAnnotationWithoutValues() {
        var singleton = AnnotationFactory.create(Singleton.class);

        assertNotNull(singleton);
        assertEquals(singleton.value(), ComputationStyle.LAZY);
    }

    @Test
    public void shouldCreateAnnotationWithValues() {
        var singleton = AnnotationFactory.create(Singleton.class, Map.of("value", ComputationStyle.EAGER));

        assertNotNull(singleton);
        assertEquals(singleton.value(), ComputationStyle.EAGER);
    }

    @Test
    public void shouldCalculateStringValue() {
        var singleton = AnnotationFactory.create(Singleton.class, Map.of("value", ComputationStyle.EAGER));

        assertNotNull(singleton);
        assertEquals(singleton.toString(), "@org.pragmatica.di.annotations.Singleton(value=EAGER)");
    }

    @Test
    public void shouldFailToCreateAnnotationWithIncorrectlyNamedValues() {
        assertThrows(InjectorException.class, () -> AnnotationFactory.create(Singleton.class, Map.of("valu", ComputationStyle.EAGER)));
    }

    @Test
    public void shouldFailToCreateAnnotationWithIncorrectValueType() throws Exception {
        assertThrows(InjectorException.class, () -> AnnotationFactory.create(Singleton.class, Map.of("value", 123)));
    }

    @Test
    public void shouldBeEqualToRealAnnotation() throws Exception {
        var singleton = AnnotationFactory.create(Singleton.class);
        var annotation = TestAnnotation.class.getAnnotation(Singleton.class);

        assertNotNull(singleton);
        assertNotNull(annotation);
        assertEquals(singleton, annotation);
        assertEquals(singleton.hashCode(), annotation.hashCode());
    }

    @Test
    public void shouldBeEqualToRealAnnotationWithArrayValues() throws Exception {
        var values = Map.of("value", new ElementType[]{ElementType.TYPE, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.METHOD});

        var target = AnnotationFactory.create(Target.class, values);
        var annotation = TestAnnotation.class.getAnnotation(Target.class);

        assertNotNull(Target.class);
        assertEquals(target, annotation);
        assertEquals(target.hashCode(), annotation.hashCode());
    }

    @Test
    public void shouldThrowExceptionIfMandatoryValueIsMissing() throws Exception {
        assertThrows(InjectorException.class, () -> AnnotationFactory.create(Target.class));
    }

    @Singleton
    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.METHOD})
    public @interface TestAnnotation {
    }
}
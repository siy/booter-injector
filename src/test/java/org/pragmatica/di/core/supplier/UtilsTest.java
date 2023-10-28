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
import org.pragmatica.di.Key;
import org.pragmatica.di.annotations.Inject;
import org.pragmatica.di.core.beans.AnnotatedConstructorClass;
import org.pragmatica.di.core.beans.DefaultConstructorClass;
import org.pragmatica.di.core.beans.MultipleConstructorClass;
import org.pragmatica.di.core.beans.SingleConstructorClass;
import org.pragmatica.di.core.exception.InjectorException;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {
    @Test
    public void shouldLocateAnnotatedConstructor() {
        Constructor<?> result = Utils.locateConstructor(Key.of(AnnotatedConstructorClass.class));

        assertNotNull(result);
        assertTrue(result.isAnnotationPresent(Inject.class));
    }

    @Test
    public void shouldLocateDefaultConstructor() {
        Constructor<?> result = Utils.locateConstructor(Key.of(DefaultConstructorClass.class));

        assertNotNull(result);
        assertEquals(result.getParameterCount(), 0);
    }

    @Test
    public void shouldLocateSingleConstructor() {
        Constructor<?> result = Utils.locateConstructor(Key.of(SingleConstructorClass.class));

        assertNotNull(result);
        assertEquals(result.getParameterCount(), 1);
    }

    @Test
    public void shouldThrowExceptionWhenMultipleNonDefaultConstructorsAreEncountered() {
        assertThrows(InjectorException.class, () -> Utils.locateConstructor(Key.of(MultipleConstructorClass.class)));
    }
}
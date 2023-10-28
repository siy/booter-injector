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
import org.pragmatica.di.Injector;
import org.pragmatica.di.core.beans.*;
import org.pragmatica.di.core.beans.tree.Bar;
import org.pragmatica.di.core.beans.tree.Ber;
import org.pragmatica.di.core.beans.tree.Foe;
import org.pragmatica.di.core.beans.tree.Foo;
import org.pragmatica.di.core.exception.InjectorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InjectorImplTest {
    private Injector getInjector() {
        return new InjectorImpl();
    }

    @Test
    public void shouldBuildSimpleHierarchyAndCreateInstance() {
        var injector = getInjector();
        var instance = injector.get(Foo.class);

        assertNotNull(instance);
        //noinspection DataFlowIssue
        assertTrue(instance.bar() instanceof Bar);
        //noinspection DataFlowIssue
        assertTrue(instance.foe() instanceof Foe);
        assertNotNull(instance.foe().parent());
        assertSame(instance.foe().parent().get(), instance);
    }

    @Test
    public void singletonInstanceShouldBeSameForSubsequentCalls() {
        var injector = getInjector();
        var instance = injector.get(Foo.class);

        assertSame(instance, injector.get(Foo.class));
    }

    @Test
    public void shouldInsertSupplierDependency() {
        var injector = getInjector();
        var instance = injector.get(BeanWithSupplierDependency.class);

        assertNotNull(instance);
        //noinspection DataFlowIssue
        assertTrue(instance.bar() instanceof Bar);
        //noinspection DataFlowIssue
        assertTrue(instance.ber() instanceof Ber);
    }

    @Test
    public void shouldCallPostConstruct() {
        var injector = getInjector();
        var instance = injector.get(BeanWithPostConstruct.class);

        assertNotNull(instance);
        assertTrue(instance.isInvoked());
        //noinspection DataFlowIssue
        assertTrue(instance.bar() instanceof Bar);
        //noinspection DataFlowIssue
        assertTrue(instance.ber() instanceof Ber);
    }

    @Test
    public void shouldConfigureBindingsViaAnnotation() {
        var injector = getInjector();
        var instance = injector.get(ListOfLongs.class);

        assertNotNull(instance);
        assertTrue(instance instanceof ListOfLongsImpl);
        assertIterableEquals(instance, List.of(91L, 82L, 73L, 64L));
    }

    @Test
    public void shouldConfigureBindingsFromMethods() {
        var injector = getInjector();
        var instance = injector.get(ListOfIntegers.class);

        assertNotNull(instance);
        assertTrue(instance instanceof ListOfIntegersImpl);
        assertIterableEquals(instance, List.of(82, 73, 91, 64));
    }

    @Test
    public void shouldBindAnnotatedParameter() {
        var injector = getInjector();
        var instance = injector.get(AnnotatedConstructorParameterClass.class);

        assertNotNull(instance);
        assertEquals(instance.value(), 42);
    }

    @Test
    public void shouldAllowManualConfiguration() {
        var injector = getInjector();
        injector.configure(AnnotatedConstructorParameterClassModule.class);

        var instance = injector.get(AnnotatedConstructorParameterClassWithoutConfiguredBy.class);

        assertNotNull(instance);
        assertEquals(instance.value(), 42);
    }

    @Test
    public void shouldThrowExceptionIfConfiguredByRefersToInterface() {
        assertThrows(InjectorException.class, () -> getInjector().get(SimpleInterface.class));
    }

    @Test
    public void shouldThrowExceptionIfNoPublicConstructorsFound() {
        assertThrows(InjectorException.class, () -> getInjector().get(ClassWithoutPublicConstructor.class));
    }

    @Test
    public void shouldThrowExceptionIfAttemptingConfigureWithNullClass() {
        assertThrows(InjectorException.class, () -> getInjector().configure(AnnotatedConstructorParameterClassModule.class, null));
    }

    @Test
    public void shouldThrowExceptionIfAttemptingConfigureWithNullClasses() {
        assertThrows(InjectorException.class, () -> getInjector().configure((Class<?>[]) null));
    }

    @Test
    public void shouldThrowExceptionIfMoreThanOneConstructorsAreAnnotated() {
        assertThrows(InjectorException.class, () -> getInjector().get(TwoAnnotatedConstructorClass.class));
    }

    @Test
    public void shouldDetectCycles() {
        assertThrows(InjectorException.class, () -> getInjector().get(ClassWithCyclicDependencies.class));
    }
}
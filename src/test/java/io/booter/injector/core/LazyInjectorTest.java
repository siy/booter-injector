package io.booter.injector.core;

import java.lang.reflect.Constructor;
import java.util.List;

import io.booter.injector.Injector;
import io.booter.injector.annotations.Inject;
import io.booter.injector.core.beans.*;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyInjectorTest {
    @Test
    public void annotatedConstructorIsLocated() throws Exception {
        Constructor<?> result = LazyInjector.locateConstructor(Key.of(LazyInjectorTest.AnnotatedConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.isAnnotationPresent(Inject.class)).isTrue();
    }

    @Test
    public void defaultConstructorIsLocated() throws Exception {
        Constructor<?> result = LazyInjector.locateConstructor(Key.of(LazyInjectorTest.DefaultConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(0);
    }

    @Test
    public void singleConstructorIsLocated() throws Exception {
        Constructor<?> result = LazyInjector.locateConstructor(Key.of(LazyInjectorTest.SingleConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(1);
    }

    @Test(expected = InjectorException.class)
    public void multipleNonDefaultConstructorsThrowException() throws Exception {
        LazyInjector.locateConstructor(Key.of(LazyInjectorTest.MultipleConstructorClass.class));
    }

    @Test
    public void simpleHierarchyIsBuiltAndInstanceIsCreated() throws Exception {
        Injector injector = new LazyInjector();
        Foo instance = injector.get(Foo.class);

        assertThat(instance).isNotNull();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.bar()).isInstanceOf(Bar.class);
        assertThat(instance.foe()).isNotNull();
        assertThat(instance.foe()).isInstanceOf(Foe.class);
        assertThat(instance.foe().parent()).isNotNull();
        assertThat(instance.foe().parent().get()).isSameAs(instance);
    }

    @Test
    public void supplierDependencyIsInsertedAsSupplier() throws Exception {
        Injector injector = new LazyInjector();
        SimpleBean2 instance = injector.get(SimpleBean2.class);

        assertThat(instance).isNotNull();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.bar()).isInstanceOf(Bar.class);
        assertThat(instance.ber()).isNotNull();
        assertThat(instance.ber()).isInstanceOf(Ber.class);
    }

    private static class AnnotatedConstructorClass {
        @Inject
        public AnnotatedConstructorClass(int j) {
        }
    }

    private static class DefaultConstructorClass {
        public DefaultConstructorClass(int param1) {
        }

        public DefaultConstructorClass() {
        }
    }

    private static class SingleConstructorClass {
        public SingleConstructorClass(int param1) {
        }
    }

    private static class MultipleConstructorClass {
        public MultipleConstructorClass(int param1) {
        }

        public MultipleConstructorClass(int param1, int param2) {
        }
    }

    private static class TypedParameterClass {
        public TypedParameterClass(List<List<String>> source) {
        }
    }
}
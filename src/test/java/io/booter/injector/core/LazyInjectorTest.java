package io.booter.injector.core;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import io.booter.injector.Injector;
import io.booter.injector.annotations.Inject;
import io.booter.injector.annotations.Singleton;
import io.booter.injector.core.beans.Bar;
import io.booter.injector.core.beans.Ber;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LazyInjectorTest {
    @Test
    public void shouldLocateAnnotatedConstructor() throws Exception {
        Constructor<?> result = LazyInjector.locateConstructor(Key.of(AnnotatedConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.isAnnotationPresent(Inject.class)).isTrue();
    }

    @Test
    public void shouldLocateDefaultConstructor() throws Exception {
        Constructor<?> result = LazyInjector.locateConstructor(Key.of(DefaultConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(0);
    }

    @Test
    public void shouldLocateSingleConstructor() throws Exception {
        Constructor<?> result = LazyInjector.locateConstructor(Key.of(SingleConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(1);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionWhenMultipleNonDefaultConstructorsAreEncountered() throws Exception {
        LazyInjector.locateConstructor(Key.of(MultipleConstructorClass.class));
    }

    @Test
    public void shouldBuildSimpleHierarchyAndCreateInstance() throws Exception {
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
    public void shouldInsertSupplierDependency() throws Exception {
        Injector injector = new LazyInjector();
        BeanWithSupplierDependency instance = injector.get(BeanWithSupplierDependency.class);

        assertThat(instance).isNotNull();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.bar()).isInstanceOf(Bar.class);
        assertThat(instance.ber()).isNotNull();
        assertThat(instance.ber()).isInstanceOf(Ber.class);
    }

    @Test
    public void shouldCallPostConstruct() throws Exception {
        Injector injector = new LazyInjector();
        BeanWithPostConstruct instance = injector.get(BeanWithPostConstruct.class);

        assertThat(instance).isNotNull();
        assertThat(instance.isInvoked()).isTrue();
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

    @Singleton
    public static class Foo {
        private final Bar bar;
        private final Foe foe;

        @Inject
        public Foo(Bar bar, Foe foe) {
            this.bar = bar;
            this.foe = foe;
        }

        public Bar bar() {
            return bar;
        }

        public Foe foe() {
            return foe;
        }
    }

    public static class Bar {
        public Bar() {
        }
    }

    @Singleton
    public static class Foe {
        private final Supplier<Foo> parent;

        @Inject
        public Foe(Supplier<Foo> parent) {
            this.parent = parent;
        }

        public Supplier<Foo> parent() {
            return parent;
        }
    }

    public static class BeanWithSupplierDependency {
        private final Supplier<Bar> bar;
        private final Ber ber;

        public BeanWithSupplierDependency(Supplier<Bar> bar, Ber ber) {
            this.bar = bar;
            this.ber = ber;
        }

        public Bar bar() {
            return bar.get();
        }

        public Ber ber() {
            return ber;
        }
    }

    public static class BeanWithPostConstruct {
        private final Bar bar;
        private final Ber ber;
        private boolean invoked = false;

        public BeanWithPostConstruct(Bar bar, Ber ber) {
            this.bar = bar;
            this.ber = ber;
        }

        public Bar bar() {
            return bar;
        }

        public Ber ber() {
            return ber;
        }

        @PostConstruct
        public void invoked() {
            this.invoked = true;
        }

        public boolean isInvoked() {
            return invoked;
        }
    }
}
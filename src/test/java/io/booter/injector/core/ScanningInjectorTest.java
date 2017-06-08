package io.booter.injector.core;

import io.booter.injector.Injector;
import io.booter.injector.core.beans.*;
import io.booter.injector.core.beans.tree.Bar;
import io.booter.injector.core.beans.tree.Ber;
import io.booter.injector.core.beans.tree.Foe;
import io.booter.injector.core.beans.tree.Foo;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ScanningInjectorTest {
    private Injector getInjector() {
        return new ScanningInjector();
    }

    @Test
    public void shouldBuildSimpleHierarchyAndCreateInstance() throws Exception {
        Injector injector = getInjector();
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
    public void singletonInstanceShouldBeSameForSubsequentCalls() throws Exception {
        Injector injector = getInjector();
        Foo instance = injector.get(Foo.class);

        assertThat(instance).isSameAs(injector.get(Foo.class));
    }

    @Test
    public void shouldInsertSupplierDependency() throws Exception {
        Injector injector = getInjector();
        BeanWithSupplierDependency instance = injector.get(BeanWithSupplierDependency.class);

        assertThat(instance).isNotNull();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.bar()).isInstanceOf(Bar.class);
        assertThat(instance.ber()).isNotNull();
        assertThat(instance.ber()).isInstanceOf(Ber.class);
    }

    @Test
    public void shouldCallPostConstruct() throws Exception {
        Injector injector = getInjector();
        BeanWithPostConstruct instance = injector.get(BeanWithPostConstruct.class);

        assertThat(instance).isNotNull();
        assertThat(instance.isInvoked()).isTrue();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.bar()).isInstanceOf(Bar.class);
        assertThat(instance.ber()).isNotNull();
        assertThat(instance.ber()).isInstanceOf(Ber.class);
    }

    @Test
    public void shouldConfigureBindingsViaAnnotation() throws Exception {
        Injector injector = getInjector();
        List<Long> instance = injector.get(ListOfLongs.class);

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ListOfLongsImpl.class);
        assertThat(instance).containsExactly(91L, 82L, 73L, 64L);
    }

    @Test
    public void shouldConfigureBindingsFromMethods() throws Exception {
        Injector injector = getInjector();
        List<Integer> instance = injector.get(ListOfIntegers.class);

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ListOfIntegersImpl.class);
        assertThat(instance).containsExactly(82, 73, 91, 64);
    }

    @Test
    public void shouldBindAnnotatedParameter() throws Exception {
        Injector injector = getInjector();

        AnnotatedConstructorParameterClass instance = injector.get(AnnotatedConstructorParameterClass.class);
        assertThat(instance).isNotNull();
        assertThat(instance.getValue()).isEqualTo(42);
    }

    @Test
    public void shouldAllowManualConfiguration() throws Exception {
        Injector injector = getInjector();

        injector.configure(AnnotatedConstructorParameterClassModule.class);

        AnnotatedConstructorParameterClassWithoutConfiguredBy instance
                = injector.get(AnnotatedConstructorParameterClassWithoutConfiguredBy.class);

        assertThat(instance).isNotNull();
        assertThat(instance.getValue()).isEqualTo(42);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfConfiguredByRefersToInterface() throws Exception {
        getInjector().get(SimpleInterface.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNoPublicConstructorsFound() throws Exception {
        getInjector().get(ClassWithoutPublicConstructor.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfAttemptingConfigureWithNullClass() throws Exception {
        getInjector().configure(AnnotatedConstructorParameterClassModule.class, null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfAttemptingConfigureWithNullClasses() throws Exception {
        getInjector().configure((Class<?>[]) null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfMoreThanOneConstructorsAreAnnotated() throws Exception {
        getInjector().get(TwoAnnotatedConstructorClass.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldDetectCycles() throws Exception {
        getInjector().get(ClassWithCyclicDependencies.class);
    }
}
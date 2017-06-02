package io.booter.injector.core;

import io.booter.injector.AbstractModule;
import io.booter.injector.Injector;
import io.booter.injector.TypeToken;
import io.booter.injector.annotations.*;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import javax.annotation.PostConstruct;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

public class FastInjectorTest {
    @Test
    public void shouldLocateAnnotatedConstructor() throws Exception {
        Constructor<?> result = FastInjector.locateConstructor(Key.of(AnnotatedConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.isAnnotationPresent(Inject.class)).isTrue();
    }

    @Test
    public void shouldLocateDefaultConstructor() throws Exception {
        Constructor<?> result = FastInjector.locateConstructor(Key.of(DefaultConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(0);
    }

    @Test
    public void shouldLocateSingleConstructor() throws Exception {
        Constructor<?> result = FastInjector.locateConstructor(Key.of(SingleConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(1);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionWhenMultipleNonDefaultConstructorsAreEncountered() throws Exception {
        FastInjector.locateConstructor(Key.of(MultipleConstructorClass.class));
    }

    @Test
    public void shouldBuildSimpleHierarchyAndCreateInstance() throws Exception {
        Injector injector = new FastInjector();
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
        Injector injector = new FastInjector();
        Foo instance = injector.get(Foo.class);

        assertThat(instance).isSameAs(injector.get(Foo.class));
    }

    @Test
    public void shouldInsertSupplierDependency() throws Exception {
        Injector injector = new FastInjector();
        BeanWithSupplierDependency instance = injector.get(BeanWithSupplierDependency.class);

        assertThat(instance).isNotNull();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.bar()).isInstanceOf(Bar.class);
        assertThat(instance.ber()).isNotNull();
        assertThat(instance.ber()).isInstanceOf(Ber.class);
    }

    @Test
    public void shouldCallPostConstruct() throws Exception {
        Injector injector = new FastInjector();
        BeanWithPostConstruct instance = injector.get(BeanWithPostConstruct.class);

        assertThat(instance).isNotNull();
        assertThat(instance.isInvoked()).isTrue();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.bar()).isInstanceOf(Bar.class);
        assertThat(instance.ber()).isNotNull();
        assertThat(instance.ber()).isInstanceOf(Ber.class);
    }

    @Test
    public void shouldBindInterfaceToImplementationManually() throws Exception {
        Injector injector = new FastInjector();
        injector.bind(Key.of(new TypeToken<List<String>>() {}), ListOfStringsImpl.class, false);

        List<String> instance = injector.get(Key.of(new TypeToken<List<String>>() {}));

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ListOfStringsImpl.class);
    }

    @Test
    public void shouldBindInterfaceToImplementationViaAnnotation() throws Exception {
        Injector injector = new FastInjector();

        List<String> instance = injector.get(ListOfStrings.class);

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ListOfStringsImpl.class);
    }

    @Test
    public void shouldConfigureBindingsViaAnnotation() throws Exception {
        Injector injector = new FastInjector();
        List<Long> instance = injector.get(ListOfLongs.class);

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ListOfLongsImpl.class);
        assertThat(instance).containsExactly(91L, 82L, 73L, 64L);
    }

    @Test
    public void shouldConfigureBindingsFromMethods() throws Exception {
        Injector injector = new FastInjector();
        List<Integer> instance = injector.get(ListOfIntegers.class);

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ListOfIntegersImpl.class);
        assertThat(instance).containsExactly(82, 73, 91, 64);
    }

    @Test
    public void shouldBindAnnotatedParameter() throws Exception {
        Injector injector = new FastInjector();

        AnnotatedConstructorParameterClass instance = injector.get(AnnotatedConstructorParameterClass.class);
        assertThat(instance).isNotNull();
        assertThat(instance.getValue()).isEqualTo(42);
    }

    @Test
    public void shouldAllowManualConfiguration() throws Exception {
        Injector injector = new FastInjector();

        injector.configure(AnnotatedConstructorParameterClassModule.class);

        AnnotatedConstructorParameterClassWithoutConfiguredBy instance
                = injector.get(AnnotatedConstructorParameterClassWithoutConfiguredBy.class);

        assertThat(instance).isNotNull();
        assertThat(instance.getValue()).isEqualTo(42);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfConfiguredByRefersToInterface() throws Exception {
        new FastInjector().get(SimpleInterface.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNoPublicConstructorsFound() throws Exception {
        new FastInjector().get(ClassWithoutPublicConstructor.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfAttemptingConfigureWithNullClass() throws Exception {
        new FastInjector().configure(AnnotatedConstructorParameterClassModule.class, null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfAttemptingConfigureWithNullClasses() throws Exception {
        new FastInjector().configure(null);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionForAttemptToAddExistingBidding() throws Exception {
        Injector injector = new FastInjector();
        injector.bind(Key.of(String.class), () -> "-", true);

        assertThat(injector.get(String.class)).isEqualTo("-");

        injector.bind(Key.of(String.class), () -> "-", true);
    }

    @Test
    public void shouldNotThrowExceptionForAttemptToAddExistingBiddingIfThrowingIsDisabled() throws Exception {
        Injector injector = new FastInjector();
        injector.bind(Key.of(String.class), () -> "-", true);

        assertThat(injector.get(String.class)).isEqualTo("-");

        injector.bind(Key.of(String.class), () -> "-", false);
    }

    @Test
    public void shouldAllowDirectManualConfigurationWithSingleton() throws Exception {
        Injector injector = new FastInjector();
        injector.bindSingleton(Key.of(List.class), ListOfIntegersImpl.class, false, false);

        List<Integer> instance = injector.get(List.class);

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ListOfIntegersImpl.class);
        assertThat(instance).containsExactly(82, 73, 91, 64);
    }

    @Test
    public void shouldAllowDirectManualConfigurationWithSupplier() throws Exception {
        Injector injector = new FastInjector();
        injector.bind(Key.of(List.class), ArrayList::new, true);

        List<Integer> instance = injector.get(List.class);

        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(ArrayList.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfMoreThanOneConstructorsAreAnnotated() throws Exception {
        new FastInjector().get(TwoAnnotatedConstructorClass.class);
    }

    //---- Test classes

    public static class ClassWithoutPublicConstructor {
        ClassWithoutPublicConstructor() {
        }
    }
    
    @ImplementedBy(SimpleInterface2.class)
    public interface SimpleInterface {
    }

    public interface SimpleInterface2 {
    }

    public static class ListOfStringsImpl extends ArrayList<String> implements ListOfStrings {
    }

    @ImplementedBy(ListOfStringsImpl.class)
    public interface ListOfStrings extends List<String> {
    }

    @ImplementedBy(ListOfLongsImpl.class)
    public interface ListOfLongs extends List<Long> {
    }

    @ImplementedBy(ListOfIntegersImpl.class)
    public interface ListOfIntegers extends List<Integer> {
    }

    @ConfiguredBy(LongListModule.class)
    public static class ListOfLongsImpl extends ArrayList<Long> implements ListOfLongs {
        public ListOfLongsImpl(Long ... initial) {
            super(Arrays.asList(initial));
        }
    }

    public static class LongListModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Long[].class).toInstance(new Long[] {91L, 82L, 73L, 64L});
        }
    }

    @ConfiguredBy(IntegerListModule.class)
    public static class ListOfIntegersImpl extends ArrayList<Integer> implements ListOfIntegers {
        public ListOfIntegersImpl(Integer ... initial) {
            super(Arrays.asList(initial));
        }
    }

    public static class IntegerListModule extends AbstractModule {
        @Override
        protected void configure() {
        }

        @Supplies
        public Integer[] getList() {
            return new Integer[] {82, 73, 91, 64};
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    @Documented
    @BindingAnnotation
    public @interface TestAnnotation {
    }

    @ConfiguredBy(AnnotatedConstructorParameterClassModule.class)
    public static class AnnotatedConstructorParameterClass {
        private final int value;

        public AnnotatedConstructorParameterClass(@TestAnnotation int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static class AnnotatedConstructorParameterClassModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(int.class).annotatedWith(TestAnnotation.class).toInstance(42);
        }
    }

    public static class AnnotatedConstructorParameterClassWithoutConfiguredBy {
        private final int value;

        public AnnotatedConstructorParameterClassWithoutConfiguredBy(@TestAnnotation int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public static class TwoAnnotatedConstructorClass {
        @Inject
        public TwoAnnotatedConstructorClass(int j) {
        }

        @Inject
        public TwoAnnotatedConstructorClass(String s) {
        }
    }

    private static class AnnotatedConstructorClass {
        @Inject
        public AnnotatedConstructorClass(int j) {
        }

    }

    private static class DefaultConstructorClass {
        @SuppressWarnings("unused")
        public DefaultConstructorClass(int param1) {
        }

        @SuppressWarnings("unused")
        public DefaultConstructorClass() {
        }
    }

    private static class SingleConstructorClass {
        @SuppressWarnings("unused")
        public SingleConstructorClass(int param1) {
        }
    }

    private static class MultipleConstructorClass {
        @SuppressWarnings("unused")
        public MultipleConstructorClass(int param1) {
        }

        @SuppressWarnings("unused")
        public MultipleConstructorClass(int param1, int param2) {
        }
    }

   @SuppressWarnings("unused")
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

    public static class Ber {
        public Ber() {
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
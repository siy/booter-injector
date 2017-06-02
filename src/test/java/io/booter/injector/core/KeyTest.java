package io.booter.injector.core;

import io.booter.injector.TypeToken;
import io.booter.injector.annotations.BindingAnnotation;
import io.booter.injector.core.beans.ClassWithDefaultConstructor;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.Supplier;
import java.lang.reflect.*;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

public class KeyTest {
    @Test
    public void shouldCreateKeyForSimpleClass() throws Exception {
        Key key = Key.of(ClassWithDefaultConstructor.class);

        assertThat(key).isNotNull();
        assertThat(key.rawClass()).isEqualTo(ClassWithDefaultConstructor.class);
        assertThat(key.annotation()).isNull();
        assertThat(key.type().toString()).isEqualTo("class io.booter.injector.core.beans.ClassWithDefaultConstructor");
        assertThat(key.toString()).isEqualTo("{io.booter.injector.core.beans.ClassWithDefaultConstructor}");
    }

    @Test
    public void shouldCreateKeyForSimpleClassWithAnnotation() throws Exception {
        Key key = Key.of(ClassWithDefaultConstructor.class, AnnotationFactory.create(TestAnnotation.class));

        assertThat(key).isNotNull();
        assertThat(key.rawClass()).isEqualTo(ClassWithDefaultConstructor.class);
        assertThat(key.annotation()).isInstanceOf(TestAnnotation.class);
        assertThat(key.toString()).isEqualTo("{io.booter.injector.core.beans.ClassWithDefaultConstructor @interface io.booter.injector.core.KeyTest$TestAnnotation}");
    }

    @Test
    public void shouldIgnoreNonBindingAnnotation() throws Exception {
        Key key = Key.of(ClassWithDefaultConstructor.class, AnnotationFactory.create(TestNonBindingAnnotation.class), AnnotationFactory.create(TestAnnotation.class));

        assertThat(key).isNotNull();
        assertThat(key.rawClass()).isEqualTo(ClassWithDefaultConstructor.class);
        assertThat(key.annotation()).isInstanceOf(TestAnnotation.class);
        assertThat(key.toString()).isEqualTo("{io.booter.injector.core.beans.ClassWithDefaultConstructor @interface io.booter.injector.core.KeyTest$TestAnnotation}");
    }

    @Test
    public void keyFollowsEqualsContract() throws Exception {
        Key key1 = Key.of(new TypeToken<List<Supplier<String>>>() {});
        Key key2 = Key.of(new TypeToken<List<Supplier<String>>>() {});

        assertThat(key1).isEqualTo(key2);
        assertThat(key2).isEqualTo(key1);

        assertThat(key1).isEqualTo(key1);
        assertThat(key2).isEqualTo(key2);

        assertThat(key1.equals("")).isFalse();
        assertThat(key2.equals("")).isFalse();
    }

    @Test
    public void shouldCreateKeyForTypeToken() throws Exception {
        Key key = Key.of(new TypeToken<List<Supplier<String>>>() {});

        assertThat(key).isNotNull();
        assertThat(key.rawClass()).isEqualTo(List.class);
        assertThat(key.annotation()).isNull();
        assertThat(key.type().toString()).isEqualTo("java.util.List<java.util.function.Supplier<java.lang.String>>");
        assertThat(key.toString()).isEqualTo("{java.util.List<java.util.function.Supplier<java.lang.String>>}");
    }

    @Test
    public void shouldCreateKeyForTypeTokenWithAnnotation() throws Exception {
        Key key = Key.of(new TypeToken<List<Supplier<String>>>() {}).with(TestAnnotation.class);

        assertThat(key).isNotNull();
        assertThat(key.rawClass()).isEqualTo(List.class);
        assertThat(key.annotation()).isInstanceOf(TestAnnotation.class);
        assertThat(key.type().toString()).isEqualTo("java.util.List<java.util.function.Supplier<java.lang.String>>");
        assertThat(key.toString()).isEqualTo("{java.util.List<java.util.function.Supplier<java.lang.String>> @interface io.booter.injector.core.KeyTest$TestAnnotation}");
    }

    @Test
    public void shouldDistinguishAnnotatedAndNotAnnotatedKeys() throws Exception {
        Key key1 = Key.of(new TypeToken<List<Supplier<String>>>() {});
        Key key2 = key1.with(TestAnnotation.class);

        assertThat(key1).isNotNull();
        assertThat(key2).isNotNull();
        assertThat(key1.rawClass()).isEqualTo(key2.rawClass());
        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    public void shouldCreateSameKeyForTypeTokenRegardlessFromTheWay() throws Exception {
        Key key1 = Key.of(new TypeToken<List<Supplier<String>>>() {}).with(TestAnnotation.class);
        Key key2 = Key.of(new TypeToken<List<Supplier<String>>>() {}, AnnotationFactory.create(TestAnnotation.class));

        assertThat(key1).isNotNull();
        assertThat(key2).isNotNull();
        assertThat(key1).isEqualTo(key2);
    }

    @Test
    public void shouldCreateKeyForSimpleParameter() throws Exception {
        Parameter parameter = getClass().getDeclaredMethod("method1", List.class).getParameters()[0];
        Key key = Key.of(parameter);

        assertThat(key).isNotNull();
        assertThat(key.toString()).isEqualTo("{java.util.List<java.lang.String>}");
    }

    @Test
    public void shouldCreateKeyForArrayType() throws Exception {
        Key key = Key.of(Supplier[].class);

        assertThat(key).isNotNull();
        assertThat(key.toString()).isEqualTo("{java.util.function.Supplier[]}");
    }

    @Test
    public void shouldCreateKeyForGenericArrayType() throws Exception {
        Key key = Key.of(new TypeToken<List<Supplier[]>>() {});

        assertThat(key).isNotNull();
        assertThat(key.toString()).isEqualTo("{java.util.List<java.util.function.Supplier[]>}");
    }

    @Test
    public void shouldCreateKeyForWildcardType() throws Exception {
        Key key = Key.of(new TypeToken<Supplier<?>>() {});

        assertThat(key).isNotNull();
        assertThat(key.toString()).isEqualTo("{java.util.function.Supplier<?>}");
    }

    public String method1(List<String> p0) {
        return (p0 == null || p0.isEmpty()) ? "empty" : p0.get(0);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfNonBindingAnnotationIsPassedToWith() throws Exception {
        Key.of(String.class).with(TestNonBindingAnnotation.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionForNullType() throws Exception {
        Key.of((Type) null);
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
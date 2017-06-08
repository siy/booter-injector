package io.booter.injector.core;

import io.booter.injector.annotations.ComputationStyle;
import io.booter.injector.annotations.Singleton;
import io.booter.injector.core.annotation.AnnotationFactory;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class AnnotationFactoryTest {
    @Test
    public void shouldCreateAnnotationWithoutValues() throws Exception {
        Singleton singleton = AnnotationFactory.create(Singleton.class);

        assertThat(singleton).isInstanceOf(Singleton.class);
        assertThat(singleton.value()).isEqualTo(ComputationStyle.LAZY);
    }

    @Test
    public void shouldCreateAnnotationWithValues() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("value", ComputationStyle.EAGER);

        Singleton singleton = AnnotationFactory.create(Singleton.class, values);

        assertThat(singleton).isInstanceOf(Singleton.class);
        assertThat(singleton.value()).isEqualTo(ComputationStyle.EAGER);
    }

    @Test
    public void shouldCalculateStringValue() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("value", ComputationStyle.EAGER);

        Singleton singleton = AnnotationFactory.create(Singleton.class, values);

        assertThat(singleton).isInstanceOf(Singleton.class);
        assertThat(singleton.toString()).isEqualTo("@io.booter.injector.annotations.Singleton(value=EAGER)");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test(expected = InjectorException.class)
    public void shouldFailToCreateAnnotationWithIncorrectlyNamedValues() throws Exception {
        Map<String, Object> values = new HashMap<>();
        //noinspection SpellCheckingInspection
        values.put("valu", ComputationStyle.EAGER);

        Singleton singleton = AnnotationFactory.create(Singleton.class, values);
        assertThat(singleton).isInstanceOf(Singleton.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldFailToCreateAnnotationWithIncorrectValueType() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("value", 123);

        Singleton singleton = AnnotationFactory.create(Singleton.class, values);
        assertThat(singleton).isInstanceOf(Singleton.class);
    }

    @Test
    public void shouldBeEqualToRealAnnotation() throws Exception {
        Singleton singleton = AnnotationFactory.create(Singleton.class);
        Singleton real = TestAnnotation.class.getAnnotation(Singleton.class);

        assertThat(singleton).isInstanceOf(Singleton.class);
        assertThat(singleton).isEqualTo(real);
        assertThat(singleton.hashCode()).isEqualTo(real.hashCode());
    }

    @Test
    public void shouldBeEqualToRealAnnotationWithArrayValues() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("value", new ElementType[] {ElementType.TYPE, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.METHOD});

        Target target = AnnotationFactory.create(Target.class, values);
        Target real = TestAnnotation.class.getAnnotation(Target.class);

        assertThat(target).isInstanceOf(Target.class);
        assertThat(target).isEqualTo(real);
        assertThat(target.hashCode()).isEqualTo(real.hashCode());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionIfMandatoryValueIsMissing() throws Exception {
        AnnotationFactory.create(Target.class);
    }

    @Singleton
    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.METHOD})
    public @interface TestAnnotation {
    }
}
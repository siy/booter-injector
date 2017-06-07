package io.booter.injector.core.supplier;

import io.booter.injector.annotations.Inject;
import io.booter.injector.core.Key;
import io.booter.injector.core.beans.AnnotatedConstructorClass;
import io.booter.injector.core.beans.DefaultConstructorClass;
import io.booter.injector.core.beans.MultipleConstructorClass;
import io.booter.injector.core.beans.SingleConstructorClass;
import io.booter.injector.core.exception.InjectorException;
import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.*;

public class UtilsTest {
    @Test
    public void shouldLocateAnnotatedConstructor() throws Exception {
        Constructor<?> result = Utils.locateConstructor(Key.of(AnnotatedConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.isAnnotationPresent(Inject.class)).isTrue();
    }

    @Test
    public void shouldLocateDefaultConstructor() throws Exception {
        Constructor<?> result = Utils.locateConstructor(Key.of(DefaultConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(0);
    }

    @Test
    public void shouldLocateSingleConstructor() throws Exception {
        Constructor<?> result = Utils.locateConstructor(Key.of(SingleConstructorClass.class));

        assertThat(result).isNotNull();
        assertThat(result.getParameterCount()).isEqualTo(1);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionWhenMultipleNonDefaultConstructorsAreEncountered() throws Exception {
        Utils.locateConstructor(Key.of(MultipleConstructorClass.class));
    }
}
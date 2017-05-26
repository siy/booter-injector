package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import io.booter.injector.core.beans.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
//TODO: cover all cases (number of parameters)
public class LambdaFactoryTest {
    private final Supplier<?>[] suppliers = new Supplier<?>[] {
            () -> 1234L,    //Long
            () -> "098",    //String
            () -> 10321,    //Integer
            () -> 2345L,    //Long
            () -> "987",    //String
            () -> 10432,    //Integer
            () -> 3456L,    //Long
            () -> "876",    //String
            () -> 10543,    //Integer
            () -> 3456L,    //Long
            };

    @Test
    public void shouldCreateBeanWith0Parameters() throws Exception {
        Supplier<Bean0> result = LambdaFactory.create(constructor(Bean0.class), suppliers);

        assertThat(result).isNotNull();
        assertThat(result.get()).isInstanceOf(Bean0.class);
    }

    @Test
    public void shouldCreateBeanWith1Parameters() throws Exception {
        Supplier<Bean1> result = LambdaFactory.create(constructor(Bean1.class), suppliers);

        assertThat(result).isNotNull();
        assertThat(result.get()).isInstanceOf(Bean1.class);
        assertThat(result.get().p0()).isEqualTo(1234L);
    }

    @Test
    public void shouldCreateBeanWith2Parameters() throws Exception {
        Supplier<Bean2> result = LambdaFactory.create(constructor(Bean2.class), suppliers);

        assertThat(result).isNotNull();
        assertThat(result.get()).isInstanceOf(Bean2.class);
        assertThat(result.get().p0()).isEqualTo(1234L);
        assertThat(result.get().p1()).isEqualTo("098");
    }

    private static <T> Constructor<T> constructor(Class<T> clazz) {
        return (Constructor<T>) clazz.getDeclaredConstructors()[0];
    }
}
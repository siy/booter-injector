package io.booter.injector.core.supplier;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import io.booter.injector.core.beans.Bar;
import io.booter.injector.core.beans.Ber;
import io.booter.injector.core.beans.SimpleBean;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodCallSupplierTest {
    @Test
    public void newInstanceIsReturned() throws Exception {
        Method method = MethodCallSupplierTest.class.getMethod("beanFactory", Bar.class, Ber.class);
        Supplier<?>[] parameters = new Supplier[] {() -> this, () -> new Bar(), () -> new Ber()};
        MethodCallSupplier<SimpleBean> supplier = new MethodCallSupplier<>(method, parameters);

        SimpleBean instance = supplier.get();
        assertThat(instance).isNotNull();
        assertThat(instance.bar()).isNotNull();
        assertThat(instance.ber()).isNotNull();
    }

    public SimpleBean beanFactory(Bar bar, Ber ber) {
        return new SimpleBean(bar, ber);
    }
}
package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodHandleInvokerTest {
    private final Supplier<?>[] parameters = new Supplier[]{
            () -> null,
            () -> 123,
            () -> 234L,
            () -> "s1",
            () -> 345,
            () -> 456L,
            () -> "s2",
            () -> 456,
            () -> 567L,
            () -> "s3",
            () -> 567,
            () -> 678L,
            () -> "s4"
    };

    private Class<?>[] types = new Class[]{
            int.class,
            long.class,
            String.class,
            int.class,
            long.class,
            String.class,
            int.class,
            long.class,
            String.class,
            int.class,
            long.class,
            String.class,
            };

    public MethodHandleInvokerTest() {
        parameters[0] = () -> this;
    }

    @Test
    public void noParametersMethodIsInvoked() throws Throwable {
        Object result = createInvokerForMethod("method0", new Supplier<?>[]{() -> this});

        assertThat(result).isInstanceOf(String.class);
        assertThat(result).isEqualTo("some value");
    }

    @Test
    public void methodsWith1ParameteIsInvoked() throws Throwable {
        createAndInvokeMethod(1);
    }

    @Test
    public void methodsWith2ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(2);
    }

    @Test
    public void methodsWith3ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(3);
    }

    @Test
    public void methodsWith4ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(4);
    }

    @Test
    public void methodsWith5ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(5);
    }

    @Test
    public void methodsWith6ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(6);
    }

    @Test
    public void methodsWith7ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(7);
    }

    @Test
    public void methodsWith8ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(8);
    }

    @Test
    public void methodsWith9ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(9);
    }

    @Test
    public void methodsWith10ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(10);
    }

    @Test
    public void methodsWith11ParametersIsInvoked() throws Throwable {
        createAndInvokeMethod(11);
    }

    private void createAndInvokeMethod(int size) throws Throwable {
        Supplier<?>[] params = Arrays.copyOf(parameters, size + 1);
        Class<?>[] paramTypes = Arrays.copyOf(types, size);

        Object result = createInvokerForMethod("method" + size, params, paramTypes);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Object[].class);

        Object[] values = (Object[]) result;

        for (int i = 0; i < size; i++) {
            assertThat(values[i]).isEqualTo(params[i + 1].get());
        }
    }

    public String method0() {
        return "some value";
    }

    public Object[] method1(int val0) {
        return new Object[]{val0};
    }

    public Object[] method2(int val0, long val1) {
        return new Object[]{val0, val1};
    }

    public Object[] method3(int val0, long val1, String val2) {
        return new Object[]{val0, val1, val2};
    }

    public Object[] method4(int val0, long val1, String val2, int val3) {
        return new Object[]{val0, val1, val2, val3};
    }

    public Object[] method5(int val0, long val1, String val2, int val3, long val4) {
        return new Object[]{val0, val1, val2, val3, val4};
    }

    public Object[] method6(int val0, long val1, String val2, int val3, long val4,
                            String val5) {
        return new Object[]{val0, val1, val2, val3, val4, val5};
    }

    public Object[] method7(int val0, long val1, String val2, int val3, long val4,
                            String val5, int val6) {
        return new Object[]{val0, val1, val2, val3, val4, val5, val6};
    }

    public Object[] method8(int val0, long val1, String val2, int val3, long val4,
                            String val5, int val6, long val7) {
        return new Object[]{val0, val1, val2, val3, val4, val5, val6, val7};
    }

    public Object[] method9(int val0, long val1, String val2, int val3, long val4,
                            String val5, int val6, long val7, String val8) {
        return new Object[]{val0, val1, val2, val3, val4, val5, val6, val7, val8};
    }

    public Object[] method10(int val0, long val1, String val2, int val3, long val4,
                             String val5, int val6, long val7, String val8, int val9) {
        return new Object[]{val0, val1, val2, val3, val4, val5, val6, val7, val8, val9};
    }

    public Object[] method11(int val0, long val1, String val2, int val3, long val4,
                             String val5, int val6, long val7, String val8, int val9, long val10) {
        return new Object[]{val0, val1, val2, val3, val4, val5, val6, val7, val8, val9, val10};
    }

    public Object[] method12(int val0, long val1, String val2, int val3, long val4,
                             String val5, int val6, long val7, String val8, int val9, long val10, String val11) {
        return new Object[]{val0, val1, val2, val3, val4, val5, val6, val7, val8, val9, val10, val11};
    }

    private Object createInvokerForMethod(String name, Supplier<?>[] suppliers, Class<?>... types) throws Throwable {
        Method method0 = getClass().getDeclaredMethod(name, types);
        MethodHandle handle0 = LambdaFactory.create(method0);
        MethodHandleInvoker methodHandleInvoker = new MethodHandleInvoker(handle0, suppliers);
        return methodHandleInvoker.invoke();
    }
}
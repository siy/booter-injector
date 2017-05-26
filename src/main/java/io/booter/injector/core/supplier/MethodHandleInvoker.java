package io.booter.injector.core.supplier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

class MethodHandleInvoker {
    private final Invoker<?> methodInvoker;

    MethodHandleInvoker(MethodHandle methodHandle, Supplier<?>[] suppliers) {
        this.methodInvoker = Invokers.with(methodHandle, suppliers);
    }

    @SuppressWarnings("unchecked")
    <T> T invoke() throws Throwable {
        return (T) methodInvoker.invoke();
    }

    private interface Invoker<T> {
        T invoke() throws Throwable;
    }

    private static class Invokers {
        static Invoker<?> with(MethodHandle handle, Supplier<?>[] args) {
            switch (args.length) {
                case 0:
                    return () -> handle.invoke();
                case 1:
                    return () -> handle.invoke(args[0].get());
                case 2:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get());
                case 3:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get());
                case 4:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get(),
                                               args[3].get());
                case 5:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get(),
                                               args[3].get(),
                                               args[4].get());
                case 6:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get(),
                                               args[3].get(),
                                               args[4].get(),
                                               args[5].get());
                case 7:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get(),
                                               args[3].get(),
                                               args[4].get(),
                                               args[5].get(),
                                               args[6].get());
                case 8:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get(),
                                               args[3].get(),
                                               args[4].get(),
                                               args[5].get(),
                                               args[6].get(),
                                               args[7].get());
                case 9:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get(),
                                               args[3].get(),
                                               args[4].get(),
                                               args[5].get(),
                                               args[6].get(),
                                               args[7].get(),
                                               args[8].get());
                case 10:
                    return () -> handle.invoke(args[0].get(),
                                               args[1].get(),
                                               args[2].get(),
                                               args[3].get(),
                                               args[4].get(),
                                               args[5].get(),
                                               args[6].get(),
                                               args[7].get(),
                                               args[8].get(),
                                               args[9].get());
                default:
                    MethodHandle spreadHandle = handle
                            .asSpreader(Object[].class, args.length)
                            .asType(MethodType.methodType(Object.class, Object[].class));

                    return () -> {
                        Object[] values = new Object[args.length];
                        for (int i = 0; i < values.length; i++) {
                            values[i] = args[i].get();
                        }
                        return spreadHandle.invoke(values);
                    };

            }
        }
    }
}

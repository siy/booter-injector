package io.booter.injector.core.supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static io.booter.injector.core.supplier.Utils.*;

public final class Suppliers {
    private Suppliers() {
    }

    public static <T> Supplier<T> factoryLazy(final Supplier<Supplier<T>> factory) {
        validateNotNull(factory);

        return new Supplier<T>() {
            private final Supplier<T> defaultDelegate = () -> init();
            private final AtomicBoolean marker = new AtomicBoolean();
            private Supplier<T> delegate = defaultDelegate;

            private T init() {
                if (marker.compareAndSet(false, true)) {
                    delegate = factory.get();
                } else {
                    while (delegate == defaultDelegate) {
                        //Intentionally left empty
                    }
                }
                return delegate.get();
            }

            public T get() {
                return delegate.get();
            }
        };
    }

    public static <T> Supplier<T> lazy(final Supplier<T> factory) {
        validateNotNull(factory);
        return factoryLazy(() -> {T instance = factory.get(); return () -> instance;});
    }

    public static <T> Supplier<T> singleton(final Supplier<T> factory, boolean eager) {
        validateNotNull(factory);

        if (eager) {
            T instance = factory.get();
            return () -> instance;
        }
        return lazy(factory);
    }

    public static <T> Supplier<T> enhancing(final Supplier<T> initial, Supplier<Supplier<T>> enhanced) {
        validateNotNull(initial, enhanced);

        return new Supplier<T>() {
            private Supplier<T> delegate = () -> step(() -> () -> step(() -> () -> step(enhanced)));

            private T step(Supplier<Supplier<T>> next) {
                T instance = initial.get();
                delegate = next.get();
                return instance;
            }

            @Override
            public T get() {
                return delegate.get();
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> methodSupplier(Method method, List<Supplier<?>> parameters) {
        validateParameters(method, parameters, 1);

        return () -> safeCall(buildMethodSupplier(method, parameters.toArray(new Supplier[parameters.size()])), method);
    }

    //TODO: used by tests only!!!
    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> methodSupplier(Method method, Supplier<?>[] parameters) {
        validateParameters(method, parameters, 1);

        return () -> safeCall(buildMethodSupplier(method, parameters), method);
    }


    private static <T> ThrowingSupplier<T> buildMethodSupplier(Method method,
                                                               Supplier<?>[] suppliers) {
        switch (method.getParameterCount()) {
            case  0: return () -> (T) method.invoke(suppliers[0].get());

            case  1: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get());

            case  2: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get());

            case  3: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get());

            case  4: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get());

            case  5: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get());

            case  6: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                          suppliers[6].get());

            case  7: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                          suppliers[6].get(), suppliers[7].get());

            case  8: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                          suppliers[6].get(), suppliers[7].get(), suppliers[8].get());

            case  9: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                     suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                     suppliers[6].get(), suppliers[7].get(), suppliers[8].get(),
                                                     suppliers[9].get());
            case 10: return () -> (T) method.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                     suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                     suppliers[6].get(), suppliers[7].get(), suppliers[8].get(),
                                                     suppliers[9].get(), suppliers[10].get());
            default:
                //Should not happen, limits are already checked
                return null;
        }
    }

    public static <T> Supplier<T> constructorSupplier(Constructor<T> constructor, Supplier<?>[] parameters) {
        validateParameters(constructor, parameters, 0);

        return () -> safeCall(buildConstructorSupplier(constructor, parameters), constructor);
    }

    public static <T> Supplier<T> constructorSupplier(Constructor<T> constructor, List<Supplier<?>> parameters) {
        validateParameters(constructor, parameters, 0);

        return () -> safeCall(buildConstructorSupplier(constructor, parameters.toArray(new Supplier[parameters.size()])), constructor);
    }

    private static <T> ThrowingSupplier<T> buildConstructorSupplier(Constructor<T> constructor,
                                                                    Supplier<?>[] suppliers) {
        switch (constructor.getParameterCount()) {
            case  0: return () -> constructor.newInstance();
            case  1: return () -> constructor.newInstance(suppliers[0].get());

            case  2: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get());

            case  3: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get());

            case  4: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get());

            case  5: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get());

            case  6: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get());

            case  7: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                          suppliers[6].get());

            case  8: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                          suppliers[6].get(), suppliers[7].get());

            case  9: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                          suppliers[6].get(), suppliers[7].get(), suppliers[8].get());

            case 10: return () -> constructor.newInstance(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                          suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                          suppliers[6].get(), suppliers[7].get(), suppliers[8].get(),
                                                          suppliers[9].get());
            default:
                //Should not happen, limits are already checked
                return null;
        }
    }

    public static <T> Supplier<T> fastConstructor(Constructor<T> constructor, Supplier<?>[] parameters) {
        return LambdaFactory.create(constructor, parameters);
    }

    public static <T> Supplier<T> fastConstructor(Constructor<T> constructor, List<Supplier<?>> parameters) {
        return LambdaFactory.create(constructor, parameters.toArray(new Supplier[parameters.size()]));
    }

    public static <T> Supplier<T> fastMethodConstructor(Method method, Supplier<?>[] parameters) {
        return LambdaFactory.create(method, parameters);
    }

    public static <T> Supplier<T> fastMethodConstructor(Method method, List<Supplier<?>> parameters) {
        return LambdaFactory.create(method, parameters.toArray(new Supplier[parameters.size()]));
    }
}
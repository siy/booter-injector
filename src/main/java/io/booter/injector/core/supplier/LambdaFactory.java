package io.booter.injector.core.supplier;

import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.function.Supplier;

import io.booter.injector.core.exception.InjectorException;

public class LambdaFactory {
    private static final Class<?> INTERFACES[] = {
            Invocable0.class, Invocable1.class, Invocable2.class, Invocable3.class, Invocable4.class,
            Invocable5.class, Invocable6.class, Invocable7.class, Invocable8.class, Invocable9.class,
            Invocable10.class
    };

    private static final int MAX_CONSTRUCTOR_PARAMETER_COUNT = INTERFACES.length - 1;

    private static Lookup LOOKUP;

    static {
        try {
            Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class, int.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }

            LOOKUP = constructor.newInstance(LambdaFactory.class, MethodHandles.Lookup.PRIVATE);
        } catch (Exception e) {
            throw new InjectorException("Unable to create new MethodHandles.Lookup instance", e);
        }
    }

    public static MethodHandle create(Method method) {
        try {
            return LOOKUP.unreflect(method);
        } catch (Exception e) {
            throw new InjectorException("Unable to create method handle for " + method, e);
        }
    }

    public static <T> Supplier<T> create(Constructor<T> constructor, Supplier<?>[] suppliers) {
        try {
            return internalCreate(constructor, suppliers);
        } catch (Throwable throwable) {
            throw new InjectorException("Unable to create lambda for " + constructor, throwable);
        }
    }

    private static <T> Supplier<T> internalCreate(Constructor<T> constructor, Supplier<?>[] suppliers) throws Throwable {
        if (constructor.getParameterCount() > suppliers.length) {
            throw new InjectorException("Provided less (" + suppliers.length
                                        + ") parameters than required for constructor " + constructor);
        }

        return createSupplier(suppliers,
                              createCallSite(constructor),
                              constructor.getParameterCount());
    }

    private static<T> CallSite createCallSite(Constructor<T> constructor) throws ReflectiveOperationException,
                                                                           LambdaConversionException {
        int parameterCount = constructor.getParameterCount();

        if (parameterCount > MAX_CONSTRUCTOR_PARAMETER_COUNT) {
            throw new InjectorException("More than " + MAX_CONSTRUCTOR_PARAMETER_COUNT
                                        + " constructor parameters are not supported in " + constructor);
        }

        MethodHandle target = LOOKUP.unreflectConstructor(constructor);

        return LambdaMetafactory.metafactory(LOOKUP, "invoke",
                                             MethodType.methodType(INTERFACES[parameterCount]),
                                             target.type().generic(),
                                             target,
                                             target.type());
    }

    private static<T> Supplier<T> createSupplier(Supplier<?>[] suppliers, CallSite callSite, int parameterCount) throws Throwable {
        switch (parameterCount) {
            case 0: {
                Invocable0<T> invocable = (Invocable0<T>) callSite.getTarget().invokeExact();
                return () -> (T) invocable.invoke();
            }
            case 1: {
                Invocable1<T, Object> invocable = (Invocable1<T, Object>) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get());
            }
            case 2: {
                Invocable2 invocable = (Invocable2) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get());
            }
            case 3: {
                Invocable3 invocable = (Invocable3) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get());
            }
            case 4: {
                Invocable4 invocable = (Invocable4) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                  suppliers[3].get());
            }
            case 5: {
                Invocable5 invocable = (Invocable5) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                  suppliers[3].get(), suppliers[4].get());
            }
            case 6: {
                Invocable6 invocable = (Invocable6) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                  suppliers[3].get(), suppliers[4].get(), suppliers[5].get());
            }
            case 7: {
                Invocable7 invocable = (Invocable7) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                  suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                  suppliers[6].get());
            }
            case 8: {
                Invocable8 invocable = (Invocable8) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                  suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                  suppliers[6].get(), suppliers[7].get());
            }
            case 9: {
                Invocable9 invocable = (Invocable9) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                  suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                  suppliers[6].get(), suppliers[7].get(), suppliers[8].get());
            }
            case 10: {
                Invocable10 invocable = (Invocable10) callSite.getTarget().invoke();
                return () -> (T) invocable.invoke(suppliers[0].get(), suppliers[1].get(), suppliers[2].get(),
                                                  suppliers[3].get(), suppliers[4].get(), suppliers[5].get(),
                                                  suppliers[6].get(), suppliers[7].get(), suppliers[8].get(),
                                                  suppliers[9].get());
            }
            default:
                //Should not happen, limits are already checked
                return null;
        }
    }

    public interface Invocable0<T> {
        T invoke();
    }

    public interface Invocable1<T, P0> {
        T invoke(P0 p0);
    }

    public interface Invocable2<T, P0, P1> {
        T invoke(P0 p0, P1 p1);
    }

    public interface Invocable3<T, P0, P1, P2> {
        T invoke(P0 p0, P1 p1, P2 p2);
    }

    public interface Invocable4<T, P0, P1, P2, P3> {
        T invoke(P0 p0, P1 p1, P2 p2, P3 p3);
    }

    public interface Invocable5<T, P0, P1, P2, P3, P4> {
        T invoke(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4);
    }

    public interface Invocable6<T, P0, P1, P2, P3, P4, P5> {
        T invoke(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    public interface Invocable7<T, P0, P1, P2, P3, P4, P5, P6> {
        T invoke(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6);
    }

    public interface Invocable8<T, P0, P1, P2, P3, P4, P5, P6, P7> {
        T invoke(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7);
    }

    public interface Invocable9<T, P0, P1, P2, P3, P4, P5, P6, P7, P8> {
        T invoke(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8);
    }

    public interface Invocable10<T, P0, P1, P2, P3, P4, P5, P6, P7, P8, P9> {
        T invoke(P0 p0, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9);
    }
}

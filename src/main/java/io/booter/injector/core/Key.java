package io.booter.injector.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

import io.booter.injector.TypeToken;
import io.booter.injector.annotations.BindingAnnotation;
import io.booter.injector.core.exception.InjectorException;

//TODO: check handling of arrays and wildcard types
public class Key {
    private final Annotation annotation;
    private final Type type;
    private final Class<?> clazz;
    private final boolean isSupplier;

    private Key(Type type, boolean isSupplier, Annotation... annotations) {
        this(type, isSupplier, failIfNull(lookupClass(type), type), findBindingAnnotation(annotations));
    }

    private Key(Type type, boolean isSupplier) {
        this(type, isSupplier, failIfNull(lookupClass(type), type), null);
    }

    private Key(Type type, boolean isSupplier, Class<?> clazz, Annotation annotation) {
        this.type = type;
        this.clazz = clazz;
        this.annotation = annotation;
        this.isSupplier = isSupplier;
    }

    public static Key of(Parameter parameter) {
        //Note that this check limits parameter type exactly to Supplier class.
        //Opposite order would allow all derived classes to be accepted which is not feasible.
        if (!parameter.getType().isAssignableFrom(Supplier.class)) {
            return new Key(parameter.getParameterizedType(), false, parameter.getAnnotations());
        }

        Type type = parameter.getParameterizedType();

        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();

            if (args.length > 0 && args[0] instanceof Class) {
                return Key.of(args[0], true);
            }
        }

        throw new InjectorException("Unable to determine parameter type for " + parameter);
    }

    public static Key of(Class<?> type) {
        return new Key(type, false);
    }

    public static Key of(Type type, Annotation... annotations) {
        return new Key(type, false, annotations);
    }

    public static Key of(Type type, boolean isSupplier, Annotation... annotations) {
        return new Key(type, isSupplier, annotations);
    }

    public static <T> Key of(TypeToken<T> token) {
        return new Key(token.type(), false);
    }

    public static <A extends Annotation> Key of(Key key, Class<A> annotation) {
        if (!annotation.isAnnotationPresent(BindingAnnotation.class)) {
            throw new InjectorException("Annotation "
                                        + annotation.getSimpleName()
                                        + " must be annotated with @"
                                        + BindingAnnotation.class.getSimpleName());
        }

        return new Key(key.type(), key.isSupplier(), key.rawClass(), AnnotationFactory.create(annotation));
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ (annotation == null ? 0x55555555: annotation.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Key)) {
            return false;
        }

        Key key = (Key) obj;
        return  type.equals(key.type) && Objects.equals(annotation, key.annotation);
    }

    public Class<?> rawClass() {
        return clazz;
    }

    public Type type() {
        return type;
    }

    public Annotation annotation() {
        return annotation;
    }

    public boolean isSupplier() {
        return isSupplier;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");

        builder.append(type.getTypeName());

        if (annotation != null) {
            builder.append(" @").append(annotation.getClass().getSimpleName());
        }

        return builder.append('}').toString();
    }

    private static Class<?> failIfNull(Class<?> clazz, Type type) {
        if (clazz == null) {
            throw new InjectorException("Unable to determine base class for "
                                        + ((type == null) ? "null" : ("type " + type)));
        }
        return clazz;
    }

    private static Annotation findBindingAnnotation(Annotation[] annotations) {
        for(Annotation annotation: annotations) {
            if(annotation.getClass().isAnnotationPresent(BindingAnnotation.class)) {
                return annotation;
            }
        }
        return null;
    }

    private static Class<?> lookupClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();

            if (rawType instanceof Class)
                return (Class<?>) rawType;
        }

        throw new InjectorException("Unable to determine base class for "
                                    + ((type == null) ? "null" : ("type " + type)));
    }
}


package io.booter.injector.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

import io.booter.injector.TypeToken;
import io.booter.injector.annotations.BindingAnnotation;
import io.booter.injector.core.exception.InjectorException;

public class Key {
    private final Annotation annotation;
    private final Type type;
    private final Class<?> clazz;
    private final boolean supplier;

    private Key(Type type, boolean supplier, Annotation... annotations) {
        this(type, supplier, lookupClass(type), findBindingAnnotation(annotations));
    }

    private Key(Type type, boolean supplier) {
        this(type, supplier, lookupClass(type), null);
    }

    private Key(Type type, boolean supplier, Class<?> clazz, Annotation annotation) {
        this.type = type;
        this.clazz = clazz;
        this.annotation = annotation;
        this.supplier = supplier;
    }

    public static Key of(Parameter parameter) {
        if (!parameter.getType().isAssignableFrom(Supplier.class)) {
            return new Key(parameter.getParameterizedType(), false, parameter.getAnnotations());
        }

        Type type = parameter.getParameterizedType();

        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();

            if (args.length > 0 && args[0] instanceof Class) {
                return Key.of(args[0], true, parameter.getAnnotations());
            }
        }

        throw new InjectorException("Unable to determine parameter type for " + parameter);
    }

    public static Key of(Type type) {
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

    public static <T> Key of(TypeToken<T> token, Annotation... annotations) {
        return new Key(token.type(), false, annotations);
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

        if (this == obj) {
            return true;
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
        return supplier;
    }

    public Key with(Class<? extends Annotation> annotation) {
        if (!annotation.isAnnotationPresent(BindingAnnotation.class)) {
            throw new InjectorException("Annotation "
                                        + annotation.getSimpleName()
                                        + " must be annotated with @"
                                        + BindingAnnotation.class.getSimpleName());
        }

        return new Key(type, supplier, clazz, AnnotationFactory.create(annotation));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");

        builder.append(type.getTypeName());

        if (annotation != null) {
            builder.append(" @").append(annotation.annotationType().toString());
        }

        return builder.append('}').toString();
    }

    private static Annotation findBindingAnnotation(Annotation[] annotations) {
        for(Annotation annotation: annotations) {
            if(annotation.annotationType().isAnnotationPresent(BindingAnnotation.class)) {
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


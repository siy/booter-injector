package io.booter.injector.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import io.booter.injector.TypeToken;
import io.booter.injector.annotations.BindingAnnotation;
import io.booter.injector.core.exception.InjectorException;

//TODO: check handling of arrays and wildcard types
public class Key {
    private final Annotation annotation;
    private final Type type;
    private final Class<?> clazz;

    private Key(Type type, Annotation... annotations) {
        this.type = type;
        this.annotation = findBindingAnnotation(annotations);
        this.clazz = failIfNull(lookupClass(type), type);
    }

    private Key(Type type, Class<?> clazz, Annotation annotation) {
        this.type = type;
        this.clazz = clazz;
        this.annotation = annotation;
    }

    public static Key of(Parameter parameter) {
        return new Key(parameter.getParameterizedType(), parameter.getAnnotations());
    }

    public static Key of(Type type, Annotation... annotations) {
        return new Key(type, annotations);
    }

    public static <T> Key of(TypeToken<T> token) {
        return new Key(token.type());
    }

    public static <A extends Annotation> Key of(Key key, Class<A> annotation) {
        if (!annotation.isAnnotationPresent(BindingAnnotation.class)) {
            throw new InjectorException("Annotation "
                                        + annotation.getSimpleName()
                                        + " must be annotated with @"
                                        + BindingAnnotation.class.getSimpleName());
        }

        return new Key(key.type(), key.rawClass(), AnnotationFactory.create(annotation));
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


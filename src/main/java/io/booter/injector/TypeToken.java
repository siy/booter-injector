package io.booter.injector;

import io.booter.injector.core.exception.InjectorException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public abstract class TypeToken<T> {
    public Type type() {
        Type type = getClass().getGenericSuperclass();

        if (!(type instanceof ParameterizedType)) {
            throw new InjectorException("Unable to determine type for TypeToken, reported type is " + type.getTypeName());
        }

        //TODO: detect wildcard types
        ParameterizedType parameterizedType = (ParameterizedType) type;
        return parameterizedType.getActualTypeArguments()[0];
    }
}

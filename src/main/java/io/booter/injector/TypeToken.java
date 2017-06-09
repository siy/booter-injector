package io.booter.injector;

import io.booter.injector.core.exception.InjectorException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Simple implementation of type token which allows to capture full generic type.
 * <br />
 * In order to use this class, one should create anonymous instance of it with required
 * type:
 * <pre> {@code
 *  new TypeToken<Map<Key, List<Values>>() {}
 * }</pre>
 *
 * Then this instance can be used to retrieve complete generic type of the created instance.
 * Note that this implementation is rudimentary and does not provide any extras, but it's good
 * fit to purposes of dependency injection.
 *
 * See http://gafter.blogspot.com/2006/12/super-type-tokens.html for more details.
 *
 * @see AbstractModule#bind(TypeToken) for examples.
 */
public abstract class TypeToken<T> {
    public Type type() {
        Type type = getClass().getGenericSuperclass();

        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        throw new InjectorException("Unable to determine type for TypeToken, reported type is " + type.getTypeName());
    }
}

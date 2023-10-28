/*
 * Copyright (c) 2017-2023 Sergiy Yevtushenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pragmatica.di;

import java.util.function.Supplier;

/**
 * This record describes binding between key and other components which can be used to create instances.
 */
public record Binding<T>(Key key, T binding, boolean resolved, boolean singleton, boolean eager) {
    /**
     * Create instance of {@link Binding} for specific implementation class. Lazy, non-singleton binding is created.
     *
     * @param key
     *          {@link Key} to which implementation class should be bound.
     * @param implementation
     *          Implementation class.
     * @return Created instance of {@link Binding}
     */
    public static Binding<Class<?>> toNonSingletonClass(Key key, Class<?> implementation) {
        return new Binding<>(key, implementation, false, false, false);
    }

    /**
     * Create instance of {@link Binding} for specific implementation class. Lazy singleton binding is created.
     *
     * @param key
     *          {@link Key} to which implementation class should be bound.
     * @param implementation
     *          Implementation class.
     * @return Created instance of {@link Binding}
     */
    public static Binding<Class<?>> toLazySingletonClass(Key key, Class<?> implementation) {
        return new Binding<>(key, implementation, false, true, false);
    }

    /**
     * Create instance of {@link Binding} for specific implementation class. Eager singleton binding is created.
     *
     * @param key
     *          {@link Key} to which implementation class should be bound.
     * @param implementation
     *          Implementation class.
     * @return Created instance of {@link Binding}
     */
    public static Binding<Class<?>> toEagerSingletonClass(Key key, Class<?> implementation) {
        return new Binding<>(key, implementation, false, true, true);
    }

    /**
     * Create binding for provided instance.
     *
     * @param key
     *         {@link Key} to which instance should be bound.
     * @param instance
     *         Instance which should be bound.
     *
     * @return Created instance of {@link Binding}
     */
    public static<T> Binding<Supplier<T>> toInstance(Key key, T instance) {
        return new Binding<>(key, () -> instance, true, true, true);
    }

    /**
     * Create binding for provided supplier.
     *
     * @param key
     *          {@link Key} to which supplier should be bound.
     * @param supplier
     *          Supplier which should be bound.
     *
     * @return Created instance of {@link Binding}
     */
    public static<T> Binding<Supplier<T>> toProvider(Key key, Supplier<T> supplier) {
        return new Binding<>(key, supplier, true, true, true);
    }
}

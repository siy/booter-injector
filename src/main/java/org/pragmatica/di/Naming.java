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

import java.util.Collections;

import org.pragmatica.di.annotations.Named;
import org.pragmatica.di.core.annotation.AnnotationFactory;
import org.pragmatica.di.core.exception.InjectorException;

/**
 * Simple helper class for run-time creation of instances of {@link Named} annotation with specified value.
 */
public final class Naming {
    private Naming() {}

    /**
     * Create new {@link Named} annotation with provided value. Note that value cannot be <code>null</code>.
     *
     * @param name
     *          Annotation value. Must not be <code>null</code>.
     * @return Created annotation instance.
     */
    public static Named with(String name) {
        if (name == null) {
            throw new InjectorException("Valuer for @Named annotation can't be null");
        }
        return AnnotationFactory.create(Named.class, Collections.singletonMap("value", name));
    }
}

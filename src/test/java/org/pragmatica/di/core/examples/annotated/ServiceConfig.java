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

package org.pragmatica.di.core.examples.annotated;

public class ServiceConfig {
    private final String name;
    private final Class<? extends Service> implementation;

    private ServiceConfig(String name, Class<? extends Service> implementation) {
        this.name = name;
        this.implementation = implementation;
    }

    public static ServiceConfig of(String name, Class<? extends Service> implementation) {
        return new ServiceConfig(name, implementation);
    }

    public String name() {
        return name;
    }

    public Class<? extends Service> implementation() {
        return implementation;
    }
}
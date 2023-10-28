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

package org.pragmatica.di.core.beans;

import jakarta.annotation.PostConstruct;
import org.pragmatica.di.core.beans.tree.Bar;
import org.pragmatica.di.core.beans.tree.Ber;

public class BeanWithPostConstruct {
    private final Bar bar;
    private final Ber ber;
    private boolean invoked = false;

    public BeanWithPostConstruct(Bar bar, Ber ber) {
        this.bar = bar;
        this.ber = ber;
    }

    public Bar bar() {
        return bar;
    }

    public Ber ber() {
        return ber;
    }

    @PostConstruct
    public void invoke() {
        this.invoked = true;
    }

    public boolean isInvoked() {
        return invoked;
    }
}
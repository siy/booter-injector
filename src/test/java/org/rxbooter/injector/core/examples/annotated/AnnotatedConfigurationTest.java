/*
 * Copyright (c) 2017 Sergiy Yevtushenko
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

package org.rxbooter.injector.core.examples.annotated;

import org.junit.Test;
import org.rxbooter.injector.Injector;

public class AnnotatedConfigurationTest {
    @Test
    public void shouldBuildInstanceTree() throws Exception {
        Injector injector = Injector.create();
        Application application = injector.get(Application.class);
        application.run();
    }
}

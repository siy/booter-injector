package io.booter.injector.core.examples.annotated;

import io.booter.injector.Injector;
import org.junit.Test;

public class AnnotatedConfigurationTest {
    @Test
    public void shouldBuildInstanceTree() throws Exception {
        Injector injector = Injector.create();
        Application application = injector.get(Application.class);
        application.run();
    }
}

package io.booter.injector.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

public class AnnotationFactory {
    public static <A extends Annotation> A create(Class<A> annotation) {
        return create(annotation, null);
    }

    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A create(Class<A> annotation, Map<String, Object> sourceValues) {
        Map<String, Object> values = sourceValues == null ? Collections.emptyMap() : sourceValues;

        return (A) Proxy.newProxyInstance(annotation.getClassLoader(),
                                          new Class[] { annotation },
                                          new AnnotationInvocationHandler(annotation, values));
    }
}

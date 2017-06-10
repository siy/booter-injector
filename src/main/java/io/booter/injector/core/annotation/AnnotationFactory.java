package io.booter.injector.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

/**
 * Utility class for creating instances of annotations at run time.
 */
public final class AnnotationFactory {
    private AnnotationFactory() {}

    /**
     * Create instance of annotation of specified annotation class. Note that annotations created with this method
     * should have no values or all values should have default values.
     *
     * @param annotation
     *          The annotation class.
     * @return Created annotation.
     */
    public static <A extends Annotation> A create(Class<A> annotation) {
        return create(annotation, null);
    }

    /**
     * Create instance of annotation of specified annotation class. Created instance will have values provided via
     * source values map. Each entry in the map has annotation method name as a key and value which should be returned
     * as an entry value. Note that all parameters are checked for presence and return type. Values should be provided
     * at least for all annotation methods which have no default value. If values are provided for annotation methods
     * which have defaults, then defaults are overridden with these values. If any value has wrong type or value for
     * any annotation method without default value is missing then
     * {@link io.booter.injector.core.exception.InjectorException} is thrown.
     *
     * @param annotation
     *          The annotation class.
     * @param sourceValues
     *          Values for annotation methods.
     * @return Created annotation.
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A create(Class<A> annotation, Map<String, Object> sourceValues) {
        Map<String, Object> values = sourceValues == null ? Collections.emptyMap() : sourceValues;

        return (A) Proxy.newProxyInstance(annotation.getClassLoader(),
                                          new Class[] { annotation },
                                          new AnnotationInvocationHandler(annotation, values));
    }
}

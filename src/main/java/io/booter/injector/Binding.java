package io.booter.injector;

/**
 * This interface describes binding between key and other components which can be used to create instances.
 */
public interface Binding<T> {
    /**
     * Key for which binding is created.
     */
    Key key();

    /**
     * Target of the binding - class or supplier.
     */
    T binding();

    /**
     * Returns <code>true</code> if no further resolution of the dependencies is necessary and <code>false</code>
     * otherwise.
     */
    boolean isResolved();

    /**
     * Returns <code>true</code> if this binding has singleton target.
     */
    boolean isSingleton();

    /**
     * Returns <code>true</code> if this binding has eager singleton target.
     */
    boolean isEager();
}

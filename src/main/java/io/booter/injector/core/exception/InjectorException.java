package io.booter.injector.core.exception;

public class InjectorException extends RuntimeException {
    public InjectorException(String message) {
        super(message);
    }

    public InjectorException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.booter.injector.core.exception;

public class InjectorException extends RuntimeException {
	private static final long serialVersionUID = -4685894495982223864L;

	public InjectorException(String message) {
        super(message);
    }

    public InjectorException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.mockobor.exceptions;


/**
 * Thrown by mockobor to indicate errors.
 */
@SuppressWarnings( "unused" )
public class MockoborException extends RuntimeException {

	/**
	 * Constructs a new mockobor exception with null as its detail message.
	 *
	 * @see RuntimeException#RuntimeException()
	 */
	public MockoborException() {}


	/**
	 * Constructs a new mockobor exception with the specified detail message.
	 *
	 * @param message the detail message.
	 * @see RuntimeException#RuntimeException(String)
	 */
	public MockoborException( String message ) {
		super( message );
	}


	/**
	 * Constructs a new mockobor exception with the specified detail message.
	 * <p>
	 * It uses {@link String#format} to formatting messages.
	 *
	 * @param message the detail message.
	 * @param args    Arguments referenced by the format specifiers in the format string.
	 * @see RuntimeException#RuntimeException(String)
	 */
	public MockoborException( String message, Object... args ) {
		super( String.format( message, args ) );
	}


	/**
	 * Constructs a new runtime exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public MockoborException( String message, Throwable cause ) {
		super( message, cause );
	}


	/**
	 * Constructs a new runtime exception with the specified cause.
	 *
	 * @param cause the cause
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public MockoborException( Throwable cause ) {
		super( cause );
	}


	/**
	 * Constructs a new runtime exception with the specified detail message, cause,
	 * suppression enabled or disabled, and writable stack trace enabled or disabled.
	 *
	 * @param message            the detail message.
	 * @param cause              the cause
	 * @param enableSuppression  whether or not suppression is enabled or disabled
	 * @param writableStackTrace whether or not the stack trace should be writable
	 * @see RuntimeException#RuntimeException(String, Throwable, boolean, boolean)
	 */
	public MockoborException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}

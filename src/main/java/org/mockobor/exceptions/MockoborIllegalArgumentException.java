package org.mockobor.exceptions;

/**
 * Thrown to indicate that a method has been passed an illegal or inappropriate argument.
 */
@SuppressWarnings( "unused" )
public class MockoborIllegalArgumentException extends MockoborException {

	/**
	 * Constructs a new mockobor exception with null as its detail message.
	 *
	 * @see RuntimeException#RuntimeException()
	 */
	public MockoborIllegalArgumentException() {
		super();
	}

	/**
	 * Constructs a new mockobor exception with the specified detail message.
	 *
	 * @param message the detail message.
	 * @see RuntimeException#RuntimeException(String)
	 */
	public MockoborIllegalArgumentException( String message ) {
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
	public MockoborIllegalArgumentException( String message, Object... args ) {
		super( message, args );
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public MockoborIllegalArgumentException( String message, Throwable cause ) {
		super( message, cause );
	}

	/**
	 * Constructs a new runtime exception with the specified cause.
	 *
	 * @param cause the cause
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public MockoborIllegalArgumentException( Throwable cause ) {
		super( cause );
	}

	public MockoborIllegalArgumentException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
		super( message, cause, enableSuppression, writableStackTrace );
	}
}

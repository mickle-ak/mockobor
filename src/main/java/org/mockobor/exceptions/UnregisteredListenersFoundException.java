package org.mockobor.exceptions;

import org.mockobor.Mockobor;


/**
 * Thrown when not unregistered listeners found (see {@link Mockobor#assertThatAllListenersAreUnregistered}).
 */
public class UnregisteredListenersFoundException extends MockoborException {

	public UnregisteredListenersFoundException( String message ) {
		super( message );
	}
}

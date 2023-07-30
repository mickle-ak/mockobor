package org.mockobor.exceptions;

import org.mockobor.listener_detectors.ListenerSelector;

import java.util.Arrays;


/**
 * Thrown when listeners to send notifications not found (if strict listeners list check sets to true).
 */
public class ListenersNotFoundException extends MockoborException {

	public ListenersNotFoundException( Class<?> listenerClass, ListenerSelector... selectors ) {
		super( "Listener registered for class '%s' with selectors %s not found",
		       listenerClass != null ? listenerClass.getSimpleName() : "null",
		       selectors != null ? Arrays.asList( selectors ) : "null" );
	}
}

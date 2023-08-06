package org.mockobor.exceptions;

import org.mockobor.listener_detectors.ListenerDefinitionDetector;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Thrown when neither of listener definition detectors can detect listener registration methods.
 */
public class ListenerRegistrationMethodsNotDetectedException extends MockoborException {

	public ListenerRegistrationMethodsNotDetectedException( Object mockedObservable,
	                                                        Collection<ListenerDefinitionDetector> detectors ) {
		super( "Neither of listener definition detectors can detect listener registration methods in object <%s> (class: %s). \n"
		       + "Used detectors: %s",
		       mockedObservable,
		       mockedObservable != null ? mockedObservable.getClass().getSimpleName() : "null",
		       detectors.stream().map( Object::getClass ).map( Class::getSimpleName ).collect( Collectors.joining( ", " ) )
		     );
	}
}

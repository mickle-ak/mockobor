package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;


/**
 * To handle registration of observer by mocked observable object.
 * <p>
 * Implementation is dependent of using mocking tool.
 */
public interface ListenerRegistrationHandler {

	/**
	 * To check if this registration handler can handle the specified mocked observable.
	 * <p>
	 * Usually this handle can handle an object if the specified object is mock created with corresponding mocking tool.
	 *
	 * @param mockedObservable object to check
	 * @return true if this handle can handle the specified object; false otherwise
	 */
	boolean canHandle( Object mockedObservable );

	/**
	 * To register stubbing in the specified mock as redirection from source method to destination method (both from Delegation).
	 *
	 * @param listeners    container with registered listeners
	 * @param registration definition of redirection as delegation from source method to destination method
	 */
	void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registration );
}

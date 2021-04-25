package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import lombok.Value;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;


/**
 * To handle registration of observer by mocked observable object.
 * <p>
 * Implementation is dependent of using mocking tool.
 */
public interface ListenerRegistrationHandler {

	/**
	 * To check if this registration handler can handle the specified mocked observable.
	 * <p>
	 * Usually this handler can handle an object if the specified object is not null and
	 * it is a mock created with corresponding mocking tool.
	 *
	 * @param mockedObservable object to check (can be null)
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


	/**
	 * To get of list of previously invoked (registration) methods.
	 * <p>
	 * Order is important and must be exactly the same as real invocation order.
	 * <p>
	 * if mocking tool does not support reading of previously invocations, then it can return empty list.
	 *
	 * @param mockedObservable object to inspect
	 * @return list of previously invocations
	 */
	default Collection<Invocation> getPreviouslyRegistrations( @NonNull Object mockedObservable ) {
		return Collections.emptyList();
	}

	/**
	 * Describes invocation of one method.
	 */
	@Value
	class Invocation {
		@NonNull Method   invokedMethod;
		@NonNull Object[] arguments;
	}
}

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
 * <p></p>
 * Implementation of this interface should be stateless, because one instance will be used for all registrations.
 */
public interface ListenerRegistrationHandler {

	/**
	 * To check if this registration handler can handle the specified mocked observable.
	 * <p>
	 * Usually this handler can handle an object if the object is not null and
	 * it is a mock created with corresponding mocking tool.
	 *
	 * @param mockedObservable object to check (can be null)
	 * @return true if this handler can handle the specified object; false otherwise
	 */
	boolean canHandle( Object mockedObservable );

	/**
	 * To create stubbing in the specified mock as a redirection from source (mocked) method to destination method of listeners container.
	 *
	 * @param listeners            container with registered listeners
	 * @param registrationDelegate definition of redirection as delegation from mocked method to method of listeners container
	 */
	void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registrationDelegate );


	/**
	 * To get of list of methods previously invoked by the specified mocked object.
	 * It is used to take over listener registrations, invoked before notifier object created.
	 * <p></p>
	 * Order is important and must be exactly the same as real invocation order!
	 * <p></p>
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

package org.mockobor.mockedobservable.mocking_tools;

import lombok.Value;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;


/**
 * To handle the registration of observer by mocked observable object.
 * <p>
 * Implementation is dependent on using mocking tool.
 * <p></p>
 * Implementation of this interface should be stateless, because one instance will be used for all registrations.
 */
public interface ListenerRegistrationHandler {

	/**
	 * To check if this registration handler can handle the specified mocked observable.
	 * <p>
	 * Usually this handler can handle an object if<ul>
	 *     <li>the object is not null, and</li>
	 *     <li>it is a mock created with corresponding mocking tool</li>
	 * </ul>
	 *
	 * @param mockedObservable object to check (can be null)
	 * @return true if this handler can handle the specified object; false otherwise
	 */
	boolean canHandle( @Nullable Object mockedObservable );

	/**
	 * To create stubbing in the specified mock as a redirection from source (mocked) method
	 * to destination method of listener container.
	 *
	 * @param listeners            container with registered listeners
	 * @param registrationDelegate definition of redirection as delegation from mocked method to method of listeners container
	 */
	void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registrationDelegate );


	/**
	 * To get of a list of methods previously invoked by the specified mocked object.
	 * It is used to take over listener registrations, invoked before a notifier object was created.
	 * <p></p>
	 * Order is important and must be exactly the same as real invocation order!
	 * <p></p>
	 * if mocking tool does not support reading of previous invocations, then it can return an empty list.
	 *
	 * @param mockedObservable object to inspect
	 * @return list of previous invocations
	 */
	default @NonNull Collection<Invocation> getPreviouslyRegistrations( @NonNull Object mockedObservable ) {
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

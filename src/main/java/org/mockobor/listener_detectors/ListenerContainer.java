package org.mockobor.listener_detectors;

import lombok.NonNull;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler;


/**
 * To store registered listeners.
 * <p>
 * {@link ListenerDefinitionDetector}s and {@link ListenerRegistrationHandler}s
 * should use it as invocation delegate to add and remove listeners.
 */
public interface ListenerContainer {

	/**
	 * Register the specified listener with the specified selector.
	 *
	 * @param selector      selector used to identify listener
	 * @param listenerClass declared class of listener (from mocked registration/add method)
	 * @param listener      listener to add
	 * @param <L>           class of listener
	 */
	<L> void addListener( @NonNull ListenerSelector selector, @NonNull Class<L> listenerClass, @NonNull L listener );

	/**
	 * Unregister the specified listener with the specified selector.
	 *
	 * @param selector      selector used to identify listener
	 * @param listenerClass declared class of listener (from mocked deregistration/remove method)
	 * @param listener      listener to remove
	 * @param <L>           class of listener
	 */
	<L> void removeListener( @NonNull ListenerSelector selector, @NonNull Class<L> listenerClass, @NonNull L listener );


	/** @return observable mock used to create this notifier (mockedObservable passed to the {@code Mockobor.createNotifierFor}). */
	@NonNull Object getObservableMock();
}

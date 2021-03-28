package de.mockobor.mockedobservable.api;

import java.util.Collection;


/**
 * Base interface for Observable-specific notifiers returned by Mockobor#start
 */
@SuppressWarnings( "unused" )
public interface ListenerNotifier {

	/** @return observable mock used to create this notifier (mockedObservable passed to the <c>startObservation( mockedObservable )</c>). */
	Object getObservableMock();


	/** @return number of currently registered listeners. */
	default int numberOfRegisteredListeners() {
		return getListeners().size();
	}


	/**
	 * To check if at least one listener was registered and all registered listeners was deregistered.
	 *
	 * @return true if some listeners was registered and all of them are deregistered; false if no listeners was registered or some of them stay registered.
	 */
	default boolean allListenersAreDeregistered() {
		return numberOfListenerRegistrations() > 0 && numberOfRegisteredListeners() == 0;
	}


	/** @return unmodifiable list of currently registered listeners. */
	Collection<Object> getListeners();


	/** @return number of listener registrations (number of calls of methods like <c>addXxxListener</c> or <c>addObserver</c> or etc.) */
	int numberOfListenerRegistrations();

	/** @return number of listener deregistrations (number of calls of methods like <c>removeXxxListener</c> or <c>deleteObserver</c> or etc.) */
	int numberOfListenerDeregistrations();
}

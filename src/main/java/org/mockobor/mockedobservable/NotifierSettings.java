package org.mockobor.mockedobservable;

import org.mockobor.Mockobor;


/**
 * Settings used to create a new notifier (used in {@link Mockobor#createNotifierFor(Object, NotifierSettings)}).
 */
public interface NotifierSettings {
	/**
	 * Flag: strict (true) or lenient (false) checking if the list of listeners selected to send notification
	 * contains any listener.
	 * <ul>
	 * <li>true - exception if no listener found in {@link ListenersNotifier#notifierFor}</li>
	 * <li>false - do nothing if no listener found in {@link ListenersNotifier#notifierFor}</li>
	 * </ul>
	 * <p>
	 * Default: true
	 *
	 * @return should be exception thrown if no listener found or not
	 * @see Mockobor#createNotifierFor(Object, NotifierSettings)
	 * @see Mockobor
	 */
	boolean getStrictCheckListenerList();


	/**
	 * Flag: should a new listener notifier implements interfaces of detected listeners.
	 * <ul>
	 * <li>
	 * true (default) - all new {@code ListenersNotifier} returned from {@link Mockobor#createNotifierFor}
	 * implement all detected listener interfaces. So events can be fired using both ways:
	 * {@code ((MyListener) notifier).somethingChanged(...);} or  {@code notifier.notifierFor( MyListener.class ).somethingChanged(...);}.
	 * </li>
	 * <li>
	 * false - all new {@code ListenersNotifier} returned from {@link Mockobor#createNotifierFor}
	 * <b>does not</b> implement listener interfaces. So there is only one way to fire events:
	 * {@code notifier.notifierFor( MyListener.class ).somethingChanged(...);}
	 * </li>
	 * </ul>
	 * <p>
	 * Default: true
	 *
	 * @return true - a new listener notifier
	 * @see Mockobor#createNotifierFor(Object, NotifierSettings)
	 * @see Mockobor
	 */
	boolean shouldNotifierImplementListenerInterfaces();
}

package org.mockobor.mockedobservable;

import org.eclipse.jdt.annotation.NonNull;


/**
 * Update methods for {@link NotifierSettings}.
 */
public interface NotifierSettingsUpdater extends NotifierSettings {

	/**
	 * To strict check if the list of listeners selected to send notification contains any listener
	 * (see {@link ListenersNotifier#notifierFor}).
	 * <p>
	 * It applies only to {@link ListenersNotifier}s created after change this setting.
	 * <p>
	 * It is a default behavior.
	 *
	 * @return itself for fluent calls
	 * @see #getStrictCheckListenerList()
	 * @see ListenersNotifier#setStrictCheckListenerList
	 */
	@NonNull NotifierSettingsUpdater strickListenerListCheck();

	/**
	 * To lenient check if the list of listeners selected to send notification contains any listener
	 * (see {@link ListenersNotifier#notifierFor}).
	 * <p>
	 * It applies only to {@link ListenersNotifier}s created after change this setting.
	 *
	 * @return itself for fluent calls
	 * @see #getStrictCheckListenerList()
	 * @see ListenersNotifier#setStrictCheckListenerList
	 */
	@NonNull NotifierSettingsUpdater lenientListenerListCheck();


	/**
	 * To allow listener notifier to implement interfaces of detected listeners.
	 * <p>
	 * It applies only to {@link ListenersNotifier}s created after change this setting.
	 * <p>
	 * It is a default behavior.
	 *
	 * @return itself for fluent calls
	 * @see #shouldNotifierImplementListenersInterfaces()
	 */
	@NonNull NotifierSettingsUpdater implementListenersInterfaces();

	/**
	 * To disallow listener notifier to implement interfaces of detected listeners.
	 * <p>
	 * It applies only to {@link ListenersNotifier}s created after change this setting.
	 *
	 * @return itself for fluent calls
	 * @see #shouldNotifierImplementListenersInterfaces()
	 */
	@NonNull NotifierSettingsUpdater ignoreListenersInterfaces();
}

package org.mockobor.listener_detectors;

import org.eclipse.jdt.annotation.NonNull;
import org.mockobor.exceptions.MockoborIllegalArgumentException;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.NotificationMethodDelegate.NotificationMethodInvocation;
import org.mockobor.mockedobservable.ObservableNotifier;
import org.mockobor.mockedobservable.PropertyChangeNotifier;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Describes detected listener(s).
 * <p>
 * It is a result of listener detection in {@link ListenerDefinitionDetector#detect} and used for
 * stabbing of registration methods in mocked observable and creation of proxy returned from {@code createNotifierFor}.
 * <p>
 * Notification delegates ({@link #getCustomNotificationMethodDelegates()}) and additional interfaces ({@link #getAdditionalInterfaces()})
 * are needed only if the implementation of {@link ListenerDefinitionDetector} provides specific support for detected listeners.
 * For example, the notifier object implements:<ul>
 * <li>{@link PropertyChangeNotifier} for {@link PropertyChangeListener} (provided from {@link PropertyChangeDetector}),</li>
 * <li>{@link ObservableNotifier} for {@code Observer} (provided from {@link ObservableDetector})</li>
 * <li>or direct listener interfaces (provided from {@link TypicalJavaListenerDetector})</li>
 * </ul>
 */
public interface ListenerDefinition {

	/** @return true if at least one listener was detected; false otherwise. */
	boolean hasListenerDetected();


	/**
	 * To get a list of detected listeners (as interfaces).
	 * <p>
	 * For example: {@code PropertyChangeListener} or {@code Observer} or {@code MyListener} etc.
	 * <p>
	 * Suitable to create proxy returned from {@code createNotifierFor}.
	 * <p></p>
	 * The class objects in the list represent only interface types.
	 *
	 * @return immutable list of detected listeners; or empty list if no interfaces detected
	 */
	@NonNull Collection<Class<?>> getDetectedListeners();


	/**
	 * To get a list of destinations for registration calls (like add/remove-Listener).
	 * <p>
	 * They used to redirect add/remove-listener methods of mocked observable to an instance of {@link ListenerContainer}.
	 * <p>
	 * Suitable to stubbing registration methods of mocked observable (used in of {@link ListenerRegistrationHandler}s).
	 *
	 * @return immutable list of registration delegates
	 */
	@NonNull Collection<RegistrationDelegate> getRegistrations();


	/**
	 * To get a list of delegations to custom implementation of notification methods.
	 * <p>
	 * Usually it is an implementation of non-default methods in additional interfaces.
	 * <p></p>
	 * Notification delegates defined here take precedence over all other implementations of source method.
	 * <p><br>
	 * Suitable to create proxy returned from {@code createNotifierFor}.
	 *
	 * @return immutable list of notification delegates or empty list if notification delegates don't require.
	 */
	@NonNull Map<Method, NotificationMethodInvocation> getCustomNotificationMethodDelegates();


	/**
	 * To get a list of additional interfaces, which should be implemented by notifier object
	 * returned from {@code createNotifierFor}
	 * (like {@link PropertyChangeNotifier} or {@link ObservableNotifier} or listener interfaces itself).
	 * <p></p>
	 * Default method implemented in additional interfaces takes precedence over other implementations of same method
	 * (exclude defined in {@link #getCustomNotificationMethodDelegates()}).
	 * <p>
	 * Implementation for non-default methods of additional interfaces must be provided in {@link #getCustomNotificationMethodDelegates()}.
	 * <p></p>
	 * Suitable to create proxy returned from {@code createNotifierFor}.
	 * <p></p>
	 * The class objects in the list represent only interface types.
	 *
	 * @return immutable list of interfaces or empty list if additional interfaces not required for the detected listeners.
	 */
	@NonNull Collection<Class<?>> getAdditionalInterfaces();


	/** Describes detected listener(s). */
	class ListenerDefinitionImpl implements ListenerDefinition {

		private final List<RegistrationDelegate> registrations = new ArrayList<>();

		private final Map<Method, NotificationMethodInvocation> notifications = new HashMap<>();

		private final Set<Class<?>> additionalInterfaces = new HashSet<>();

		private final Set<Class<?>> detectedListeners = new HashSet<>();


		@Override
		public boolean hasListenerDetected() {
			return !detectedListeners.isEmpty();
		}

		@Override
		public @NonNull Collection<Class<?>> getDetectedListeners() {
			return Collections.unmodifiableCollection( detectedListeners );
		}

		/**
		 * To add detected listener (as interface).
		 *
		 * @param listenerClasses list of listener's class (as declared in registration method). Must represent an interface type.
		 * @throws MockoborImplementationError if at least one of the specified listener classes does not represent an interface type
		 * @see #getDetectedListeners()
		 */
		public void addDetectedListeners( @NonNull Collection<Class<?>> listenerClasses ) throws MockoborImplementationError {
			if( !listenerClasses.stream().allMatch( Class::isInterface ) ) {
				throw new MockoborImplementationError( "Unexpected: only interfaces allowed here (was: %s)", listenerClasses );
			}
			detectedListeners.addAll( listenerClasses );
		}


		@Override
		public @NonNull Collection<RegistrationDelegate> getRegistrations() {
			return Collections.unmodifiableCollection( registrations );
		}

		/**
		 * To add delegate for registration method.
		 *
		 * @param registration registration delegate to add
		 * @see #getRegistrations()
		 */
		public void addRegistration( @NonNull RegistrationDelegate registration ) {
			registrations.add( registration );
		}


		@Override
		public @NonNull Map<Method, NotificationMethodInvocation> getCustomNotificationMethodDelegates() {
			return Collections.unmodifiableMap( notifications );
		}

		/**
		 * To add delegate for notification method.
		 *
		 * @param delegate delegate for notification method to add
		 * @see #getCustomNotificationMethodDelegates()
		 */
		public void addNotification( @NonNull NotificationMethodDelegate delegate ) {
			notifications.put( delegate.getSource(), delegate.getDestination() );
		}


		@Override
		public @NonNull Collection<Class<?>> getAdditionalInterfaces() {
			return Collections.unmodifiableCollection( additionalInterfaces );
		}

		/**
		 * To additional interface, which should be implemented by notifier object returned from {@code createNotifierFor}.
		 *
		 * @param ifaces list of additional interfaces to add
		 * @throws MockoborIllegalArgumentException if at least one of the specified classes does not represent an interface type
		 * @see #getAdditionalInterfaces()
		 */
		public void addAdditionalInterfaces( @NonNull Collection<Class<?>> ifaces ) throws MockoborIllegalArgumentException {
			if( !ifaces.stream().allMatch( Class::isInterface ) ) {
				throw new MockoborIllegalArgumentException( "only interface allowed here (but was: %s)", ifaces );
			}
			additionalInterfaces.addAll( ifaces );
		}
	}
}

package org.mockobor;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.jdt.annotation.NonNull;
import org.mockobor.exceptions.ListenerRegistrationMethodsNotDetectedException;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.exceptions.UnregisteredListenersFoundException;
import org.mockobor.listener_detectors.ListenerDefinitionDetector;
import org.mockobor.listener_detectors.ListenerSelector;
import org.mockobor.mockedobservable.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Observable;
import java.util.Observer;


/**
 * Main Mockobor class.
 *
 * It es a static facade for Mockobor functionality.
 * <p><br>
 * <H1><b>Send events from mocked observable object to test object.</b></H1>
 * <p><br>
 * You can use {@link Mockobor#createNotifierFor} to simulate sending of notification events from mocked observable object
 * to a test object, that register listeners by the mocked observable object.
 * <p><br>
 * Example for Java-style listeners:
 * <pre class="code"><code class="java">
 *
 * // The listener to listen changes on something
 * public interface MyListener {
 * 	void somethingChanged1( Object someNewValue );
 * 	void somethingChanged2( Object someNewValue, Object anotherValue );
 * }
 *
 * // The class (that support notification on change something) to mock.
 * public interface ObservableObject {
 * 	void addMyListener( MyListener listener );
 * 	void addMyListener( Object selector, MyListener listener ); // selector like property name in PropertyChangeSupport
 * 	void removeMyListener( MyListener listener );
 * 	void removeMyListener( Object selector, MyListener listener );
 * }
 *
 * class ObservableObjectTest {
 * ...
 *      // create notifier for mocked ObservableObject
 *      ObservableObject mockedObservableObject = mock( ObservableObject.class )
 *      ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservableObject );
 *
 *      // tested object registers itself as listener by the specified ObservableObject object
 *      TestObject testObject = new TestObject( mockedObservableObject );
 *
 *      // send events to testObject (standard way):
 *      ((MyListener) notifier).somethingChanged1( newValue );
 *      ((MyListener) notifier).somethingChanged2( newValue1, newValue2 );
 *
 *      // send events to testObject (another way, it is exactly the same as above):
 *      notifier.notifierFor( MyListener.class ).somethingChanged1( newValue );
 *      notifier.notifierFor( MyListener.class ).somethingChanged2( newValue1, newValue2 );
 *
 *      // if you need to select listeners registered with certain qualifier, then you can do it with "selector"-s:
 *      notifier.notifierFor( "selector", MyListener.class ).somethingChanged1( newValue );
 *      notifier.notifierFor( MyListener.class, selector( "selector" ) ).somethingChanged1( newValue ); // exactly as above
 *
 *      // to notify (send the same event to) all listeners registered with selector "s1" OR selector "s2":
 *      notifier.notifierFor( MyListener.class, selector( "s1" ), selector( "s2" ) ).somethingChanged1( newValue );
 *
 *      // send events to testObject using "empty selector" (it is exactly the same as "((MyListener) notifier).somethingChanged1( newValue );"
 *      // because registration without any qualifier is the same as registration with empty selector):
 *      notifier.notifierFor( MyListener.class, selector() ).somethingChanged1( newValue );
 *      notifier.notifierFor( MyListener.class, selector() ).somethingChanged2( newValue1, newValue2 );
 * ...
 * }
 * </code></pre>
 * See {@link ListenersNotifier#notifierFor(Class)}, {@link ListenersNotifier#notifierFor(Object, Class)} or
 * {@link ListenersNotifier#notifierFor(Class, ListenerSelector...)} for more details.
 * <p><br>
 * For {@link PropertyChangeSupport} and {@link Observable} Mockobor has a special support -
 * {@link PropertyChangeNotifier}, {@link ObservableNotifier}.
 * <p><br>
 * Example of usage of {@link PropertyChangeNotifier} for mocked {@link PropertyChangeSupport}:
 * <pre class="code"><code class="java">
 *
 * // create notifier for mocked PropertyChangeSupport
 * PropertyChangeSupport mockedPropertyChangeSupport = mock( PropertyChangeSupport.class )
 * PropertyChangeNotifier notifier = (PropertyChangeNotifier) Mockobor.createNotifierFor( mockedPropertyChangeSupport );
 *
 * // tested object registers itself as listener by the specified PropertyChangeSupport object
 * TestObject testObject = new TestObject( mockedPropertyChangeSupport );
 *
 * // send events to testObject
 * notifier.firePropertyChange( "myProperty", oldValue, newValue );
 * </code></pre>
 * <p>
 * Example of usage of {@link ObservableNotifier} for mocked {@link Observable}:
 * <pre class="code"><code class="java">
 *
 * // create notifier for mocked Observable
 * Observable mockedObservable = mock( Observable.class )
 * ObservableNotifier notifier = (ObservableNotifier) Mockobor.createNotifierFor( mockedObservable );
 *
 * // tested object registers itself as listener by the specified Observable object
 * TestObject testObject = new TestObject( mockedObservable );
 *
 * // send events to testObject
 * notifier.notifyObservers( updateParameter );
 * </code></pre>
 * <H1><b>Check if all listeners are unregistered.</b></H1>
 * <p><br>
 * You can use Mockobor to check if all registered by mocked observable object listeners are unregistered:
 * <pre class="code"><code class="java">
 *
 * // create notifier for mocked PropertyChangeSupport
 * ObservableObject mockedObservableObject = mock( ObservableObject.class )
 * ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservableObject );
 *
 * // tested object registers itself as listener by the specified PropertyChangeSupport object
 * TestObject testObject = new TestObject( mockedObservableObject );
 *
 * // tested object should remove itself from the specified PropertyChangeSupport object on close.
 * testObject.close(); // or dispose() etc.
 *
 * // check that all listeners are unregistered
 * Mockobor.assertThatAllListenersAreUnregistered( notifier );
 * </code></pre>
 * See {@link ListenersNotifier#allListenersAreUnregistered()} for details.
 * <p></p>
 * For more details see tests:<ul>
 * <li>
 * <a href="https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_BeanPropertyChange_Test.java">
 * UsageExample_BeanPropertyChange_Test</a>
 * </li>
 * <li>
 * <a href="https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_Observable_Test.java">
 * UsageExample_Observable_Test</a>
 * </li>
 * <li>
 * <a href="https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_TypicalJavaListener_Test.java">
 * UsageExample_TypicalJavaListener_Test</a>
 * </li>
 * <li>
 * <a href="https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_CustomDetector_Test.java">
 * UsageExample_CustomDetector_Test</a>
 * </li>
 * <li>
 * <a href="https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_MockitoAnnotation_Test.java">
 * UsageExample_MockitoAnnotation_Test</a>
 * </li>
 * <li>
 * <a href="https://github.com/mickle-ak/mockobor/blob/master/src/test/java/org/mockobor/mockedobservable/UsageExample_allListenersAreUnregistered_Test.java">
 * UsageExample_allListenersAreUnregistered_Test</a>
 * </li>
 * </ul>
 *
 * @see #createNotifierFor
 * @see ListenersNotifier
 * @see ListenersNotifier#allListenersAreUnregistered()
 * @see ListenerSelector
 * @see PropertyChangeNotifier
 * @see ObservableNotifier
 */
@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class Mockobor {

	private static final NotifierFactory NOTIFIER_FACTORY = new NotifierFactory( MockoborContext.LISTENER_DETECTORS_REGISTRY,
	                                                                             MockoborContext.MOCKING_TOOLS_REGISTRY );

	// ==================================================================================
	// ============================== Listener notifier =================================
	// ==================================================================================

	/**
	 * To create notifier for the specified mocked observable.
	 * <p></p>
	 * This method does follow:<ul>
	 * <li>searchs for registration (add/remove listener) methods for all (known) observer/listeners,</li>
	 * <li>detect used mocking tool,</li>
	 * <li>redirect add/remove-listeners methods from mocked object to itself (using detected mocking tool) and</li>
	 * <li>creates dynamic proxy as notifier object.</li>
	 * </ul>
	 * <p></p>
	 * According to the specified mocked observable the returned notifier implements follow interfaces (in any combinations): <ul>
	 * <li>{@link ObservableNotifier} + {@link Observer} if methods for {@code Observer} registration found:<ul>
	 * <li>{@code addObserver(Observer)}</li>
	 * <li>{@code deleteObserver(Observer)}</li></ul>
	 * </li>
	 * <li>{@link PropertyChangeNotifier} + {@link PropertyChangeListener} if methods for {@code PropertyChangeListener} registration found:<ul>
	 * <li>{@code addPropertyChangeListener([String,]PropertyChangeListener)}</li>
	 * <li>{@code removePropertyChangeListener([String,]PropertyChangeListener)}</li></ul>
	 * </li>
	 * <li>{@code XxxListener} (typical java listener) if methods for {@code XxxListener} registration found:<ul>
	 * <li>{@code addXxxListener([selectors,]XxxListener)}</li>
	 * <li>{@code removeXxxListener([selectors,]XxxListener)}</li></ul>
	 * </li>
	 * <li>another kind of observer provided by custom {@link ListenerDefinitionDetector}s<br>
	 * (see {@link MockoborContext#registerListenerDefinitionDetector(ListenerDefinitionDetector)})
	 * </li></ul>
	 * <p></p>
	 * It uses globally settings statically stored in {@link MockoborContext} (use {@link MockoborContext#updateNotifierSettings()} to
	 * change global settings).
	 * <p>
	 * Invocation of these methods is equal to call {@code Mockobor.createNotifierFor(mockedObservable, Mockobor.notifierSettings())}.
	 * <p></p>
	 * See {@link Mockobor} for usage examples.
	 *
	 * @param mockedObservable mock of observable object
	 * @return notifier used to simulate notification calls from the specified mocked observable
	 * @throws ListenerRegistrationMethodsNotDetectedException if neither of listener definition detectors can detect listener registration methods
	 * @throws MockingToolNotDetectedException                 if the specified object is not a mock or used mocking tool does not support
	 * @see Mockobor
	 * @see MockoborContext
	 * @see #createNotifierFor(Object, NotifierSettings)
	 */
	public static @NonNull ListenersNotifier createNotifierFor( @NonNull Object mockedObservable )
			throws ListenerRegistrationMethodsNotDetectedException, MockingToolNotDetectedException {
		return createNotifierFor( mockedObservable, notifierSettings() );
	}

	/**
	 * To create notifier for the specified mocked observable with the specified settings.
	 * <p></p>
	 * This method does follow:<ul>
	 * <li>searchs for registration (add/remove listener) methods for all (known) observer/listeners,</li>
	 * <li>detect used mocking tool,</li>
	 * <li>redirect add/remove-listeners methods from mocked object to itself (using detected mocking tool) and</li>
	 * <li>creates dynamic proxy as notifier object.</li>
	 * </ul>
	 * <p></p>
	 * Typical usage:
	 * <pre class="code"><code class="java">
	 * ObservableObject mockedObservableObject = mock( ObservableObject.class )
	 * NotifierSettings settings = Mockobor.notifierSettings().lenientListenerListCheck()
	 * ListenerNotifier notifier = Mockobor.createNotifierFor( mockedObservableObject, settings );
	 * </code></pre>
	 * <p>
	 * See {@link Mockobor} and {@link #createNotifierFor(Object)} for more usage examples.
	 * <p>
	 * See {@link NotifierSettings} and {@link NotifierSettingsUpdater} for possible settings.
	 *
	 * @param mockedObservable mock of observable object
	 * @param settings         settings used to create a new listener notifier
	 * @return notifier used to simulate notification calls from the specified mocked observable
	 * @throws ListenerRegistrationMethodsNotDetectedException if neither of listener definition detectors can detect listener registration methods
	 * @throws MockingToolNotDetectedException                 if the specified object is not a mock or used mocking tool does not support
	 * @see #notifierSettings()
	 * @see Mockobor
	 * @see #createNotifierFor(Object)
	 */
	public static @NonNull ListenersNotifier createNotifierFor( @NonNull Object mockedObservable, @NonNull NotifierSettings settings )
			throws ListenerRegistrationMethodsNotDetectedException, MockingToolNotDetectedException {
		return NOTIFIER_FACTORY.create( mockedObservable, settings );
	}


	/** @return a new copy of {@link NotifierSettings} created on base of settings statically stored in {@link MockoborContext}. */
	public static @NonNull NotifierSettingsUpdater notifierSettings() {
		return MockoborContext.notifierSettingsImpl.toBuilder().build();
	}


	/**
	 * To check if all listeners by all specified notifiers are unregistered.
	 * <p><br>
	 * Example:
	 * <pre class="code"><code class="java">
	 *
	 * // create mocks of observable objects
	 * PropertyChangeSupport mockedPropertyChangeSupport = mock( PropertyChangeSupport.class );
	 * Observable mockedObservable = mock( Observable.class );
	 * AnotherObservable mockedAnotherObservable = mock( AnotherObservable.class );
	 *
	 * // create notifier for mocked observable
	 * ListenersNotifier notifier1 = Mockobor.createNotifierFor( mockedPropertyChangeSupport );
	 * ListenersNotifier notifier2 = Mockobor.createNotifierFor( mockedObservable );
	 * ListenersNotifier notifier3 = Mockobor.createNotifierFor( mockedAnotherObservable );
	 *
	 * // tested object registers itself as listener by the specified objects
	 * TestObject testObject = new TestObject( mockedPropertyChangeSupport, mockedObservable, mockedAnotherObservable );
	 *
	 * // tested object should remove itself from the specified in constructor objects on close.
	 * testObject.close(); // or dispose() etc.
	 *
	 * // check that all listeners are unregistered
	 * Mockobor.assertThatAllListenersAreUnregistered( notifier1, notifier2, notifier3 );
	 *
	 * </code></pre>
	 *
	 * @param notifiers notifiers to check
	 * @throws UnregisteredListenersFoundException if some specified notifier contains unregistered listener(s)
	 */
	public static void assertThatAllListenersAreUnregistered( @NonNull ListenersNotifier... notifiers )
			throws UnregisteredListenersFoundException {
		NOTIFIER_FACTORY.assertThatAllListenersAreUnregistered( notifiers );
	}
}

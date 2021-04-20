package org.mockobor;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.mockobor.exceptions.ListenerRegistrationMethodsNotDetectedException;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.listener_detectors.ListenerDefinitionDetector;
import org.mockobor.listener_detectors.ListenerSelector;
import org.mockobor.mockedobservable.ListenersNotifier;
import org.mockobor.mockedobservable.NotifierFactory;
import org.mockobor.mockedobservable.ObservableNotifier;
import org.mockobor.mockedobservable.PropertyChangeNotifier;

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
 *      // if you need to select listeners registered with certain values, then you can do it with "selector"-s:
 *      notifier.notifierFor( "selector", MyListener.class ).somethingChanged1( newValue );
 *      notifier.notifierFor( MyListener.class, selector( "selector" ) ).somethingChanged1( newValue ); // exactly as above
 *
 *      // to notify (send the same event to) all listeners registered with selector "s1" OR selector "s2":
 *      notifier.notifierFor( MyListener.class, selector( "s1" ), selector( "s2" ) ).somethingChanged1( newValue );
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
 * notifier.firePropertyChange( "myPropery", oldValue, newValue );
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
 * assertThat( notifier.allListenersAreUnregistered() ).as( "all listeners are unregistered" ).isTrue();
 * </code></pre>
 * See {@link ListenersNotifier#allListenersAreUnregistered()} for details.
 * <p></p>
 * For more details see tests:<ul>
 * <li>UsageExample_Observable_Test</li>
 * <li>UsageExample_BeanPropertyChange_Test</li>
 * <li>UsageExample_TypicalJavaListener_Test</li>
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

	/**
	 * To create notifier for the specified mocked observable.
	 * <p>
	 * It searchs for registration (add/remove listener) methods for all possible observer/listeners,
	 * detect used mocking tool,
	 * redirect add/remove-listeners methods from mocked object to itself (using detected mocking tool) and
	 * creates dynamic proxy as notifier object.
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
	 * <p>
	 * See javadoc of {@link Mockobor} for usage examples.
	 *
	 * @param mockedObservable mock of observable object
	 * @return notifier used to simulate notification calls from the specified mocked observable
	 * @throws ListenerRegistrationMethodsNotDetectedException if neither of listener definition detectors can detect listener registration methods
	 * @throws MockingToolNotDetectedException                 if the specified object not a mock or mocking tool used to mock it not supported
	 * @see MockoborContext
	 */
	@NonNull
	public static ListenersNotifier createNotifierFor( @NonNull Object mockedObservable )
			throws ListenerRegistrationMethodsNotDetectedException, MockingToolNotDetectedException {
		return new NotifierFactory( MockoborContext.LISTENER_DETECTORS_REGISTRY,
		                            MockoborContext.MOCKING_TOOLS_REGISTRY )
				.create( mockedObservable );
	}
}

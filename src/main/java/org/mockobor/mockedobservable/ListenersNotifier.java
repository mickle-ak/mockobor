package org.mockobor.mockedobservable;

import lombok.NonNull;
import org.mockobor.Mockobor;
import org.mockobor.exceptions.ListenersNotFoundException;
import org.mockobor.listener_detectors.ListenerSelector;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Observable;

import static org.mockobor.listener_detectors.ListenerSelector.selector;


/**
 * Base interface for observable-specific notifiers returned by {@link Mockobor#createNotifierFor}.
 * <p>
 * Usually it does not needed to use methods of this interface (probably except {@link #allListenersAreUnregistered()}).
 * <p>
 * In same cases (if you listeners have methods with same signature or you need selectors (see {@link ListenerSelector})
 * to register/find your listeners) your can use:<ul>
 * <li>{@link #notifierFor(Class)}</li>
 * <li>{@link #notifierFor(Object, Class)}</li>
 * <li>{@link #notifierFor(Class, ListenerSelector...)}</li>
 * </ul>
 */
@SuppressWarnings( "unused" )
public interface ListenersNotifier {


	/**
	 * To check if at least one listener was registered and all registered listeners was unregistered.
	 * <p><br>
	 * Example (using Mockito, AssertJ, PropertyChangeSupport):
	 * <pre class="code"><code class="java">
	 *
	 * // create notifier for mocked PropertyChangeSupport
	 * PropertyChangeSupport mockedPropertyChangeSupport = mock( PropertyChangeSupport.class )
	 * ListenersNotifier notifier = Mockobor.createNotifierFor( mockedPropertyChangeSupport );
	 *
	 * // tested object registers itself as listener by the specified PropertyChangeSupport object
	 * TestObject testObject = new TestObject( mockedPropertyChangeSupport );
	 *
	 * // tested object should remove itself from the specified PropertyChangeSupport object on close.
	 * testObject.close(); // or dispose() etc.
	 *
	 * // check that all listeners are unregistered
	 * assertThat( notifier.allListenersAreUnregistered() ).as( "all listeners are unregistered" ).isTrue();
	 *
	 * </code></pre>
	 * For other listeners it works exactly like.
	 *
	 * @return true if some listeners was registered and all of them are deregistered; false if no listeners was registered or some of them stay registered.
	 */
	default boolean allListenersAreUnregistered() {
		return numberOfListenerRegistrations() > 0 && numberOfRegisteredListeners() == 0;
	}


	/**
	 * To get notifier proxy which implements the specified listener interface and
	 * can be used to call listener's methods for all listeners registered without selectors.
	 * <p><br>
	 * If listeners with suitably selector not found (was not added), then (per default) it throws {@code ListenersNotFoundException}.<br>
	 * This behaviour can be changed by set strictCheckListenerList-flag ({@link #setStrictCheckListenerList}).
	 * <p><br>
	 * If listener's notification method returns some value, then result of last called notification methods will be returned (invocation order is undefined).
	 * <p><br>
	 * It is equal to call {@code notifierFor( listenerClass, selector() )}.
	 * See {@link #notifierFor(Class, ListenerSelector...)} for details.
	 *
	 * @param listenerClass class of required listener
	 * @param <L>           class of required listener
	 * @return a object which implements the specified listener interface to call its methods for notification
	 * @throws ListenersNotFoundException if no listeners with suitably selector registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see #notifierFor(Class, ListenerSelector...)
	 */
	default <L> @NonNull L notifierFor( @NonNull Class<L> listenerClass )
			throws ListenersNotFoundException {
		return notifierFor( listenerClass, selector() );
	}

	/**
	 * To get notifier proxy which implements the specified listener interface and
	 * can be used to call listener's methods for all listeners registered with the specified selector value.
	 * <p><br>
	 * If listeners with suitably selector not found (was not added), then (per default) it throws {@code ListenersNotFoundException}.<br>
	 * This behaviour can be changed by set strictCheckListenerList-flag ({@link #setStrictCheckListenerList}).
	 * <p><br>
	 * If listener's notification method returns some value, then result of last called notification methods will be returned (invocation order is undefined).
	 * <p><br>
	 * It is equal to call {@code notifierFor( listenerClass, selector(listenerSelector) )}.
	 * See {@link #notifierFor(Class, ListenerSelector...)} for details.
	 *
	 * @param listenerClass    class of required listener
	 * @param listenerSelector selector used to registration of listeners
	 * @param <L>              class of required listener
	 * @return a object which implements the specified listener interface to call its methods for notification
	 * @throws ListenersNotFoundException if no listeners with suitably selector registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see #notifierFor(Class, ListenerSelector...)
	 */
	default <L> @NonNull L notifierFor( @NonNull Object listenerSelector, @NonNull Class<L> listenerClass )
			throws ListenersNotFoundException {
		return notifierFor( listenerClass, selector( listenerSelector ) );
	}

	/**
	 * To get notifier proxy which implements the specified listener interface and <br>
	 * can be used to call listener's methods for all listeners registered with at least one of the specified selectors.
	 * <p><br>
	 * If listeners with suitably selector not found (was not added), then (per default) it throws {@code ListenersNotFoundException}.<br>
	 * This behaviour can be changed by set strictCheckListenerList-flag ({@link #setStrictCheckListenerList}).
	 * <p><br>
	 * If listener's notification method returns some value, then result of last called notification methods will be returned (invocation order is undefined).
	 * <p><br>
	 * Usually you don't need to use this method, because <ul>
	 * <li>{@code createNotifierFor} returns object, that already implemented all found listeners interfaces and</li>
	 * <li>the most listeners are registered without selectors</li>
	 * </ul>
	 * So you can send notifications to registered listeners directly by cast this object (returned from {@code createNotifierFor}) <br>
	 * to required listener interface and call one of its methods (for examples see {@link Mockobor}).
	 * <p>
	 * For some common cases ({@link Observable}, {@link PropertyChangeListener})
	 * Mockobor has special support: {@link ObservableNotifier}, {@link PropertyChangeNotifier}.
	 * <p><br>
	 * But, if you listeners have methods with same signature or you need selectors (see {@link ListenerSelector})
	 * to register/find your listeners, you can use {@code notifierFor}-methods to notify only required listener(s).
	 * <p>
	 * For Example, for {@link PropertyChangeListener} you need (sometimes) to define String parameter - property name.
	 * A call of {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)} can be simulates so:
	 * <pre class="code"><code class="java">
	 *
	 * // Test object
	 * public class TestObject implement PropertyChangeListener {
	 *     public TestObject( PropertyChangeSupport pcs ) {
	 *         pcs.addPropertyChangeListener( "myProperty", this ); // (#1) registration for specific property == registration with selector "myProperty"
	 *         pcs.addPropertyChangeListener( this ); // (#2) registration for all properties == registration without selector
	 *     }
	 * }
	 *
	 * public class TestObjectTest {
	 *  ...
	 *      // create notifier for mocked PropertyChangeSupport
	 *      PropertyChangeSupport mockedPropertyChangeSupport = mock( PropertyChangeSupport.class )
	 *      ListenersNotifier listenersNotifier = Mockobor.createNotifierFor( mockedPropertyChangeSupport);
	 *
	 *      // tested object registers itself as listener by the specified PropertyChangeSupport object
	 *      TestObject testObject = new TestObject(mockedPropertyChangeSupport);
	 *
	 *      // get special instance of PropertyChangeListener used to notify all listeners registered with follow selectors:
	 *      // - selector("myProperty") - for specific property "myProperty" (#1)
	 *      // - selector() - for all properties (#2)
	 *      PropertyChangeListener propertyChangeNotifier = listenersNotifier.notifierFor(PropertyChangeListener.class, selector(), selector("myProperty"));
	 *      propertyChangeNotifier.propertyChange(new PropertyChangeEvent(...));
	 *  ...
	 * }
	 * </code></pre>
	 * Note: This code is only example to explicitation of {@code notifierFor} and {@code ListenerSelector}s.<br>
	 * For PropertyChangeListener Mockobor has special support - {@link PropertyChangeNotifier}.
	 *
	 * @param listenerClass class of required listener
	 * @param selectors     selectors used by registration of listeners
	 * @param <L>           class of required listener
	 * @return a object which implements the specified listener interface to call its methods for notification
	 * @throws ListenersNotFoundException if no listeners with suitably selector registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 */
	@NonNull <L> L notifierFor( @NonNull Class<L> listenerClass, @NonNull ListenerSelector... selectors ) 
			throws ListenersNotFoundException;


	/**
	 * Set flag: strict check of listener list by notification.<ul>
	 * <li>true - exception if no listener found</li>
	 * <li>false - do nothing if no listener found</li>
	 * </ul>
	 *
	 * @param strict should throw exception if no listener found
	 */
	void setStrictCheckListenerList( boolean strict );


	/** @return observable mock used to create this notifier (mockedObservable passed to the {@code createNotifierFor( mockedObservable )}). */
	@NonNull Object getObservableMock();


	/** @return number of listener registrations (number of calls of methods like {@code addXxxListener} or {@code addObserver} or etc.) */
	int numberOfListenerRegistrations();

	/** @return number of listener deregistrations (number of calls of methods like {@code removeXxxListener} or {@code deleteObserver} or etc.) */
	int numberOfListenerDeregistrations();


	/** @return number of currently registered listeners. */
	int numberOfRegisteredListeners();


	/**
	 * Get list of all currently registered listeners.
	 *
	 * @return unmodifiable list of all currently registered listeners, or empty list if nothing found
	 */
	@NonNull Collection<Object> getAllListeners();


	/**
	 * Get list of currently registered listeners of required type (with any selectors).
	 *
	 * @param listenerClass class of required listener
	 * @param <L>           class of required listener
	 * @return unmodifiable list of listeners of required type, or empty list if nothing found
	 */
	@NonNull <L> Collection<L> getListeners( @NonNull Class<L> listenerClass );

	/**
	 * Get list of listeners of required type, registered with one of the specified selectors.
	 *
	 * @param listenerClass class of required listener
	 * @param selectors     selectors used by registration of listeners
	 * @param <L>           class of required listener
	 * @return unmodifiable list of listeners of required type, registered with one of the specified selectors, or empty list if nothing found
	 */
	@NonNull <L> Collection<L> getListeners( @NonNull Class<L> listenerClass, @NonNull ListenerSelector... selectors );
}

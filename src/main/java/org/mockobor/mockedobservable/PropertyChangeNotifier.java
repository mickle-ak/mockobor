package org.mockobor.mockedobservable;

import org.eclipse.jdt.annotation.NonNull;
import org.mockobor.Mockobor;
import org.mockobor.exceptions.ListenersNotFoundException;
import org.mockobor.listener_detectors.ListenerSelector;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import static org.mockobor.listener_detectors.ListenerSelector.selector;


/**
 * Interface to simulate notification calls from mocked beans that support bound properties (see {@link PropertyChangeSupport}).
 * <p></p>
 * Created from {@link Mockobor#createNotifierFor} if the specified observable object has methods like:<ul>
 * <li>{@code void addPropertyChangeListener(PropertyChangeListener listener)} or</li>
 * <li>{@code void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)}</li>
 * </ul>
 * <p></p>
 * Example:
 *
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
 * notifier.firePropertyChange( null, oldValue, newValue );
 *
 * </code></pre>
 */
@SuppressWarnings( "unused" )
public interface PropertyChangeNotifier extends ListenersNotifier {

	/**
	 * To simulate call of {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)} by mocked bean.
	 * <p>
	 * Reports a bound property update to listeners that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if old and new values are equal and non-null.
	 * <p>
	 * This is merely a convenience wrapper around the more general {@link #firePropertyChange(PropertyChangeEvent)} method.
	 *
	 * @param propertyName the programmatic name of the property that was changed
	 * @param oldValue     the old value of the property
	 * @param newValue     the new value of the property
	 * @throws ListenersNotFoundException if no {@code PropertyChangeListener} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
	 */
	default void firePropertyChange( String propertyName, Object oldValue, Object newValue )
			throws ListenersNotFoundException {
		if( oldValue == null || !oldValue.equals( newValue ) ) {
			firePropertyChange( new PropertyChangeEvent( getObservableMock(), propertyName, oldValue, newValue ) );
		}
	}

	/**
	 * To simulate call of {@link PropertyChangeSupport#firePropertyChange(String, int, int)} by mocked bean.
	 * <p>
	 * Reports a bound property update to listeners that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if old and new values are equal.
	 * <p>
	 * This is merely a convenience wrapper around the more general {@link #firePropertyChange(String, Object, Object)} method.
	 *
	 * @param propertyName the programmatic name of the property that was changed
	 * @param oldValue     the old value of the property
	 * @param newValue     the new value of the property
	 * @throws ListenersNotFoundException if no {@code PropertyChangeListener} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see PropertyChangeSupport#firePropertyChange(String, int, int)
	 */
	default void firePropertyChange( String propertyName, int oldValue, int newValue )
			throws ListenersNotFoundException {
		if( oldValue != newValue ) {
			firePropertyChange( propertyName, Integer.valueOf( oldValue ), Integer.valueOf( newValue ) );
		}
	}

	/**
	 * To simulate call of {@link PropertyChangeSupport#firePropertyChange(String, boolean, boolean)} by mocked bean.
	 * <p>
	 * Reports a boolean bound property update to listeners that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if old and new values are equal.
	 * <p>
	 * This is merely a convenience wrapper around the more general {@link #firePropertyChange(String, Object, Object)}  method.
	 *
	 * @param propertyName the programmatic name of the property that was changed
	 * @param oldValue     the old value of the property
	 * @param newValue     the new value of the property
	 * @throws ListenersNotFoundException if no {@code PropertyChangeListener} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see PropertyChangeSupport#firePropertyChange(String, boolean, boolean)
	 */
	default void firePropertyChange( String propertyName, boolean oldValue, boolean newValue )
			throws ListenersNotFoundException {
		if( oldValue != newValue ) {
			firePropertyChange( propertyName, Boolean.valueOf( oldValue ), Boolean.valueOf( newValue ) );
		}
	}


	/**
	 * To simulate call of {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)} by mocked bean.
	 * <p>
	 * Fires a property change event to listeners that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if the given event's old and new values are equal and non-null.
	 *
	 * @param event the {@code PropertyChangeEvent} to be fired
	 * @throws ListenersNotFoundException if no {@code PropertyChangeListener} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)
	 */
	default void firePropertyChange( @NonNull PropertyChangeEvent event )
			throws ListenersNotFoundException {
		ListenerSelector[] selectors = event.getPropertyName() != null
		                               ? new ListenerSelector[]{ selector(), selector( event.getPropertyName() ) } // common + named
		                               : new ListenerSelector[]{ selector() }; // common only

		notifierFor( PropertyChangeListener.class, selectors ).propertyChange( event );
	}


	/**
	 * To simulate call of {@link PropertyChangeSupport#fireIndexedPropertyChange(String, int, Object, Object)} by mocked bean.
	 * <p>
	 * Reports a bound-indexed property update to listeners
	 * that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if old and new values are equal and non-null.
	 * <p>
	 * This is merely a convenience wrapper around the more general {@link #firePropertyChange(PropertyChangeEvent)} method.
	 *
	 * @param propertyName the programmatic name of the property that was changed
	 * @param index        the index of the property element that was changed
	 * @param oldValue     the old value of the property
	 * @param newValue     the new value of the property
	 * @throws ListenersNotFoundException if no {@code PropertyChangeListener} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see PropertyChangeSupport#fireIndexedPropertyChange(String, int, Object, Object)
	 */
	default void fireIndexedPropertyChange( String propertyName, int index, Object oldValue, Object newValue )
			throws ListenersNotFoundException {
		if( oldValue == null || !oldValue.equals( newValue ) ) {
			firePropertyChange( new IndexedPropertyChangeEvent( getObservableMock(), propertyName, oldValue, newValue, index ) );
		}
	}

	/**
	 * To simulate call of {@link PropertyChangeSupport#fireIndexedPropertyChange(String, int, int, int)} by mocked bean.
	 * <p>
	 * Reports an integer-bound indexed property update to listeners
	 * that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if old and new values are equal.
	 * <p>
	 * This is merely a convenience wrapper around the more general {@link #fireIndexedPropertyChange(String, int, Object, Object)} method.
	 *
	 * @param propertyName the programmatic name of the property that was changed
	 * @param index        the index of the property element that was changed
	 * @param oldValue     the old value of the property
	 * @param newValue     the new value of the property
	 * @throws ListenersNotFoundException if no {@code PropertyChangeListener} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see PropertyChangeSupport#fireIndexedPropertyChange(String, int, int, int)
	 */
	default void fireIndexedPropertyChange( String propertyName, int index, int oldValue, int newValue )
			throws ListenersNotFoundException {
		if( oldValue != newValue ) {
			fireIndexedPropertyChange( propertyName, index, Integer.valueOf( oldValue ), Integer.valueOf( newValue ) );
		}
	}

	/**
	 * To simulate call of {@link PropertyChangeSupport#fireIndexedPropertyChange(String, int, boolean, boolean)} by mocked bean.
	 * <p>
	 * Reports a boolean bound indexed property update to listeners that have been registered to track updates of all properties or a property with the
	 * specified name.
	 * <p>
	 * No event is fired if old and new values are equal.
	 * <p>
	 * This is merely a convenience wrapper around the more general {@link #fireIndexedPropertyChange(String, int, Object, Object)} method.
	 *
	 * @param propertyName the programmatic name of the property that was changed
	 * @param index        the index of the property element that was changed
	 * @param oldValue     the old value of the property
	 * @param newValue     the new value of the property
	 * @throws ListenersNotFoundException if no {@code PropertyChangeListener} registered by the mocked observable and
	 *                                    strictCheckListenerList-flag set to true (default)
	 * @see PropertyChangeSupport#fireIndexedPropertyChange(String, int, boolean, boolean)
	 */
	default void fireIndexedPropertyChange( String propertyName, int index, boolean oldValue, boolean newValue )
			throws ListenersNotFoundException {
		if( oldValue != newValue ) {
			fireIndexedPropertyChange( propertyName, index, Boolean.valueOf( oldValue ), Boolean.valueOf( newValue ) );
		}
	}


	/**
	 * Check if there are any listeners for a specific property, including those registered on all properties.
	 * If <code>propertyName</code> is null, only check for listeners registered on all properties.
	 *
	 * @param propertyName the property name.
	 * @return true if there are one or more listeners for the given property
	 * @see PropertyChangeSupport#hasListeners(String)
	 */
	default boolean hasListeners( String propertyName ) {
		return numberOfRegisteredListeners() > 0
		       && ( !getListeners( PropertyChangeListener.class, selector() ).isEmpty() ||
		            propertyName != null && !getListeners( PropertyChangeListener.class, selector( propertyName ) ).isEmpty() );
	}

	/**
	 * Returns an array of all the listeners that were added to the mocked bean with addPropertyChangeListener().
	 *
	 * @return all of the <code>PropertyChangeListeners</code> added or an empty array if no listeners have been added
	 * @see PropertyChangeSupport#getPropertyChangeListeners()
	 */
	@NonNull
	default PropertyChangeListener[] getPropertyChangeListeners() {
		PropertyChangeListener[] empty = new PropertyChangeListener[0];
		return numberOfRegisteredListeners() > 0
		       ? getListeners( PropertyChangeListener.class ).toArray( empty )
		       : empty;
	}

	/**
	 * Returns an array of all the listeners which have been associated with the named property.
	 *
	 * @param propertyName The name of the property being listened to
	 * @return all of the <code>PropertyChangeListeners</code> associated with the named property.
	 * 		If no such listeners have been added, or if <code>propertyName</code> is null, an empty array is returned.
	 * @see PropertyChangeSupport#getPropertyChangeListeners(String)
	 */
	@NonNull
	default PropertyChangeListener[] getPropertyChangeListeners( String propertyName ) {
		PropertyChangeListener[] empty = new PropertyChangeListener[0];
		return propertyName != null && numberOfRegisteredListeners() > 0
		       ? getListeners( PropertyChangeListener.class, selector( propertyName ) ).toArray( empty )
		       : empty;
	}
}

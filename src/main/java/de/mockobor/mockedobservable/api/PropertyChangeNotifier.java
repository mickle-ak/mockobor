package de.mockobor.mockedobservable.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Interface to simulate notification calls from mocked beans that support bound properties (see {@link PropertyChangeSupport}).
 */
@SuppressWarnings( "unused" )
public interface PropertyChangeNotifier extends ListenerNotifier {

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
	 * @see PropertyChangeSupport#firePropertyChange(String, Object, Object)
	 */
	void firePropertyChange( String propertyName, Object oldValue, Object newValue );

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
	 * @see PropertyChangeSupport#firePropertyChange(String, int, int)
	 */
	void firePropertyChange( String propertyName, int oldValue, int newValue );

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
	 * @see PropertyChangeSupport#firePropertyChange(String, boolean, boolean)
	 */
	void firePropertyChange( String propertyName, boolean oldValue, boolean newValue );


	/**
	 * To simulate call of {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)} by mocked bean.
	 * <p>
	 * Fires a property change event to listeners that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if the given event's old and new values are equal and non-null.
	 *
	 * @param event the {@code PropertyChangeEvent} to be fired
	 * @see PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)
	 */
	void firePropertyChange( PropertyChangeEvent event );


	/**
	 * To simulate call of {@link PropertyChangeSupport#fireIndexedPropertyChange(String, int, Object, Object)} by mocked bean.
	 * <p>
	 * Reports a bound indexed property update to listeners that have been registered to track updates of all properties or a property with the specified name.
	 * <p>
	 * No event is fired if old and new values are equal and non-null.
	 * <p>
	 * This is merely a convenience wrapper around the more general {@link #firePropertyChange(PropertyChangeEvent)} method.
	 *
	 * @param propertyName the programmatic name of the property that was changed
	 * @param index        the index of the property element that was changed
	 * @param oldValue     the old value of the property
	 * @param newValue     the new value of the property
	 * @see PropertyChangeSupport#fireIndexedPropertyChange(String, int, Object, Object)
	 */
	void fireIndexedPropertyChange( String propertyName, int index, Object oldValue, Object newValue );

	/**
	 * To simulate call of {@link PropertyChangeSupport#fireIndexedPropertyChange(String, int, int, int)} by mocked bean.
	 * <p>
	 * Reports an integer bound indexed property update to listeners that have been registered to track updates of all properties or a property with the
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
	 * @see PropertyChangeSupport#fireIndexedPropertyChange(String, int, int, int)
	 */
	void fireIndexedPropertyChange( String propertyName, int index, int oldValue, int newValue );

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
	 * @see PropertyChangeSupport#fireIndexedPropertyChange(String, int, boolean, boolean)
	 */
	void fireIndexedPropertyChange( String propertyName, int index, boolean oldValue, boolean newValue );


	/**
	 * Check if there are any listeners for a specific property, including those registered on all properties.
	 * If <code>propertyName</code> is null, only check for listeners registered on all properties.
	 *
	 * @param propertyName the property name.
	 * @return true if there are one or more listeners for the given property
	 * @see PropertyChangeSupport#hasListeners(String)
	 */
	boolean hasListeners( String propertyName );

	/**
	 * Returns an array of all the listeners that were added to the mocked bean with addPropertyChangeListener().
	 *
	 * @return all of the <code>PropertyChangeListeners</code> added or an empty array if no listeners have been added
	 * @see PropertyChangeSupport#getPropertyChangeListeners()
	 */
	PropertyChangeListener[] getPropertyChangeListeners();

	/**
	 * Returns an array of all the listeners which have been associated with the named property.
	 *
	 * @param propertyName The name of the property being listened to
	 * @return all of the <code>PropertyChangeListeners</code> associated with the named property.
	 * 		If no such listeners have been added, or if <code>propertyName</code> is null, an empty array is returned.
	 * @see PropertyChangeSupport#getPropertyChangeListeners(String)
	 */
	PropertyChangeListener[] getPropertyChangeListeners( String propertyName );
}

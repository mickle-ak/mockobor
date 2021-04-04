package org.mockobor.listener_detectors;

import lombok.NonNull;
import org.mockobor.Mockobor;
import org.mockobor.mockedobservable.PropertyChangeNotifier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.Collection;


/**
 * To detect if the mocked object can be used as {@link PropertyChangeSupport} (the mocked object is a beans that supports bound properties).
 * <p><br>
 * It means that the mocked object has follow methods:<ul>
 * <li><code>addPropertyChangeListener({@link PropertyChangeListener})</code></li>
 * <li><code>addPropertyChangeListener(String, {@link PropertyChangeListener})</code></li>
 * <li><code>removePropertyChangeListener({@link PropertyChangeListener})</code></li>
 * <li><code>removePropertyChangeListener(String, {@link PropertyChangeListener})</code></li>
 * </ul>
 * (deregistration is optional)
 * <p></p>
 * In this case <ul>
 * <li>a notifier object returned from {@link Mockobor#createNotifierFor} implements
 * {@link PropertyChangeNotifier} and {@link PropertyChangeListener}</li>
 * <li>the found registration methods will be redirected to the notifier object</li>
 * </ul>
 */
public class PropertyChangeDetector implements ListenerDefinitionDetector {

	@Override
	public @NonNull ListenersDefinition detect( @NonNull Collection<Method> methods ) {
		return null;
	}
}

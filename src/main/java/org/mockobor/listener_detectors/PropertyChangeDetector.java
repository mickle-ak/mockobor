package org.mockobor.listener_detectors;

import lombok.NonNull;
import org.mockobor.Mockobor;
import org.mockobor.mockedobservable.PropertyChangeNotifier;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;


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
public class PropertyChangeDetector extends AbstractDetector implements ListenerDefinitionDetector {

	@Override
	protected boolean isListenerClass( @NonNull Class<?> parameterType, @NonNull Method method ) {
		return PropertyChangeListener.class.isAssignableFrom( parameterType );
	}

	@Override
	protected boolean isAddMethods( @NonNull Method method ) {
		return ReflectionUtils.methodMatch( method, "addPropertyChangeListener", PropertyChangeListener.class )
		       || ReflectionUtils.methodMatch( method, "addPropertyChangeListener", String.class, PropertyChangeListener.class );
	}

	@Override
	protected boolean isRemoveMethods( @NonNull Method method ) {
		return ReflectionUtils.methodMatch( method, "removePropertyChangeListener", PropertyChangeListener.class )
		       || ReflectionUtils.methodMatch( method, "removePropertyChangeListener", String.class, PropertyChangeListener.class );
	}

	@Override
	protected @NonNull List<Class<?>> getAdditionalInterfaces() {
		return Collections.singletonList( PropertyChangeNotifier.class );
	}
}

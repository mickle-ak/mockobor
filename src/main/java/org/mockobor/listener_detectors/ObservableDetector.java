package org.mockobor.listener_detectors;

import lombok.NonNull;
import org.mockobor.Mockobor;
import org.mockobor.mockedobservable.ObservableNotifier;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;


/**
 * To detect if the mocked object can be used as {@link Observable}.
 * <p><br>
 * It means that the mocked object has follow registration methods:<ul>
 * <li><code>addObserver({@link Observer})</code></li>
 * <li><code>deleteObserver({@link Observer})*</code></li>
 * </ul>
 * (deregistration is optional)
 * <p></p>
 * In this case <ul>
 * <li>a notifier object returned from {@link Mockobor#createNotifierFor} implements
 * {@link ObservableNotifier} and {@link Observer}</li>
 * <li>the found registration methods will be redirected to the notifier object</li>
 * </ul>
 */
public class ObservableDetector implements ListenerDefinitionDetector {

	@Override
	public @NonNull ListenersDefinition detect( @NonNull Collection<Method> methods ) {
		return null;
	}
}

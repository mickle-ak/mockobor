package org.mockobor.listener_detectors;

import lombok.NonNull;
import org.mockobor.Mockobor;
import org.mockobor.mockedobservable.ObservableNotifier;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
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
public class ObservableDetector extends AbstractDetector implements ListenerDefinitionDetector {

	@Override
	protected boolean isListenerClass( @NonNull Class<?> parameterType, @NonNull Method method ) {
		return Observer.class.isAssignableFrom( parameterType );
	}

	@Override
	protected boolean isAddMethods( @NonNull Method method ) {
		return ReflectionUtils.methodMatch( method, "addObserver", Observer.class );
	}

	@Override
	protected boolean isRemoveMethods( @NonNull Method method ) {
		return ReflectionUtils.methodMatch( method, "deleteObserver", Observer.class );
	}

	@Override
	protected @NonNull List<Class<?>> getAdditionalInterfaces() {
		return Collections.singletonList( ObservableNotifier.class );
	}
}

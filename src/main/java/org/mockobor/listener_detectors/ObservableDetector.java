package org.mockobor.listener_detectors;

import org.eclipse.jdt.annotation.NonNull;
import org.mockobor.Mockobor;
import org.mockobor.mockedobservable.ObservableNotifier;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Observer;


/**
 * To detect if the mocked object can be used as {@code Observable}.
 * <p><br>
 * It means that the mocked object has follow registration methods:<ul>
 * <li><code>addObserver({@code Observer})</code></li>
 * <li><code>deleteObserver({@code Observer})*</code></li>
 * </ul>
 * (deregistration is optional)
 * <p></p>
 * In this case <ul>
 * <li>a notifier object returned from {@link Mockobor#createNotifierFor} implements
 * {@link ObservableNotifier} and {@code Observer}</li>
 * <li>the found registration methods will be redirected to the notifier object</li>
 * </ul>
 */
public class ObservableDetector extends AbstractDetector implements ListenerDefinitionDetector {

	@SuppressWarnings( "deprecation" )
	@Override
	protected boolean isListenerClass( @NonNull Class<?> parameterType, @NonNull Method method ) {
		return parameterType.equals( Observer.class );
	}

	@SuppressWarnings( "deprecation" )
	@Override
	protected boolean isAddMethods( @NonNull Method method ) {
		return ReflectionUtils.methodMatch( method, "addObserver", Observer.class );
	}

	@SuppressWarnings( "deprecation" )
	@Override
	protected boolean isRemoveMethods( @NonNull Method method ) {
		return ReflectionUtils.methodMatch( method, "deleteObserver", Observer.class );
	}

	@Override
	protected @NonNull List<Class<?>> getAdditionalInterfaces() {
		return Collections.singletonList( ObservableNotifier.class );
	}
}

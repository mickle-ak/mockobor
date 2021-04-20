package org.mockobor.listener_detectors;


import lombok.NonNull;
import org.mockobor.Mockobor;

import java.lang.reflect.Method;
import java.util.regex.Pattern;


/**
 * To detect if the mocked object allows registration of listeners as it typical in java.
 * <p><br>
 * It means that the mocked object has follow methods:<ul>
 * <li>{@code addXxxListener(XxxListener)}</li>
 * <li>{@code addXxxListener(listener selection, XxxListener)}</li>
 * <li>{@code removeXxxListener(XxxListener)}</li>
 * <li>{@code removeXxxListener(listener selection, XxxListener)}</li>
 * </ul>
 * (deregistration is optional)
 * <p></p>
 * In this case <ul>
 * <li>a notifier object returned from {@link Mockobor#createNotifierFor} implements {@code XxxListener}</li>
 * <li>the found registration methods will be redirected to the notifier object</li>
 * </ul>
 */
public class TypicalJavaListenerDetector extends AbstractDetector implements ListenerDefinitionDetector {

	private final Pattern listenerClassNamePattern;
	private final Pattern addMethodNamePattern;
	private final Pattern removeMethodNamePattern;

	public TypicalJavaListenerDetector() {
		this( ".*Listener", "add(.*)Listeners?", "remove(.*)Listeners?" );
	}


	public TypicalJavaListenerDetector( @NonNull String listenerClassNameRegexp,
	                                    @NonNull String addMethodNameRegexp,
	                                    @NonNull String removeMethodNameRegexp ) {
		this.listenerClassNamePattern = Pattern.compile( listenerClassNameRegexp );
		this.addMethodNamePattern = Pattern.compile( addMethodNameRegexp );
		this.removeMethodNamePattern = Pattern.compile( removeMethodNameRegexp );
	}

	@Override
	protected boolean isListenerClass( @NonNull Class<?> parameterType, @NonNull Method method ) {
		return listenerClassNamePattern.matcher( parameterType.getSimpleName() ).matches();
	}

	@Override
	protected boolean isAddMethods( @NonNull Method method ) {
		return addMethodNamePattern.matcher( method.getName() ).matches();
	}

	@Override
	protected boolean isRemoveMethods( @NonNull Method method ) {
		return removeMethodNamePattern.matcher( method.getName() ).matches();
	}
}

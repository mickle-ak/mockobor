package org.mockobor.listener_detectors;


import lombok.NonNull;
import org.mockobor.Mockobor;

import java.lang.reflect.Method;
import java.util.Collection;


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
public class TypicalJavaListenerDetector implements ListenerDefinitionDetector {

	@Override
	public @NonNull ListenersDefinition detect( @NonNull Collection<Method> methods ) {
		return null;
	}
}

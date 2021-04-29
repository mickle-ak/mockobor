package org.mockobor.listener_detectors;

import org.eclipse.jdt.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.Collection;


/**
 * It finds methods used to registration of listeners and creates corresponding listeners definition.
 * <p></p>
 * Implementation Notes:
 * <p></p>
 * Implementation of this interface should be stateless, because one instance will be used for all detections.
 * <p></p>
 * Typically to implement this interface you only need to extend {@link AbstractDetector} and override required:<ul>
 * <li>{@code isListenerClass(Class, Method)}</li>
 * <li>{@code isAddMethods(Method)}</li>
 * <li>{@code isRemoveMethods(Method)}</li>
 * </ul>
 * and optional:<ul>
 * <li>{@code getAdditionalInterfaces()}</li>
 * <li>{@code getNotificationDelegates()}</li>
 * </ul>
 * methods.
 *
 * @see ObservableDetector
 * @see PropertyChangeDetector
 * @see TypicalJavaListenerDetector
 */
public interface ListenerDefinitionDetector {

	/**
	 * To find methods used for registration of listeners and creates corresponding listeners definition.<p>
	 * If some listeners was detected, then {@link ListenersDefinition#hasListenerDetected()} should return true;<p>
	 * If listeners was not detected, then {@link ListenersDefinition#hasListenerDetected()} should return false;
	 *
	 * @param methods readonly list of methods to examine (defined in mocked observable)
	 * @return definition of detected listeners.
	 */
	@NonNull ListenersDefinition detect( @NonNull Collection<Method> methods );
}

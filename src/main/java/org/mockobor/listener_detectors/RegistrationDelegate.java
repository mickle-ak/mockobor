package org.mockobor.listener_detectors;

import lombok.NonNull;
import lombok.Value;

import java.lang.reflect.Method;


/**
 * Describe delegation from (mocked) registration method (add/remove listener) to destination method of {@link ListenerContainer}.
 */
@Value
public class RegistrationDelegate {

	/** Method of mocked object - source of redirection. */
	@NonNull Method source;

	/** Destination of registration as a function. */
	@NonNull RegistrationInvocation destination;


	/** Function used as destination of registration method (add/remove listeners). */
	@FunctionalInterface
	public interface RegistrationInvocation {

		/**
		 * To invoke delegation method.
		 *
		 * @param listeners invocation destination (container with registered listeners)
		 * @param method    invoked registration method
		 * @param arguments arguments passed to invoked registration method
		 * @return result of invocation
		 */
		Object invoke( @NonNull ListenerContainer listeners, @NonNull Method method, @NonNull Object... arguments );
	}
}

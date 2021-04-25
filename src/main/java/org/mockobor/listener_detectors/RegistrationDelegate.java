package org.mockobor.listener_detectors;

import lombok.NonNull;
import lombok.Value;

import java.lang.reflect.Method;


/**
 * Describe delegation from (mocked) registration method to destination method of {@link ListenerContainer}.
 */
@Value
public class RegistrationDelegate {

	@NonNull Method source;

	@NonNull RegistrationInvocation destination;


	/**
	 * Specified destination as function with arguments passed to invoked method.
	 */
	@FunctionalInterface
	public interface RegistrationInvocation {

		/**
		 * To invoke delegation method.
		 *
		 * @param listeners invocation destination (container with registered listeners)
		 * @param method    invoked method
		 * @param arguments arguments passed to invoked method
		 * @return result of invocation
		 */
		Object invoke( @NonNull ListenerContainer listeners, @NonNull Method method, @NonNull Object... arguments );
	}

}

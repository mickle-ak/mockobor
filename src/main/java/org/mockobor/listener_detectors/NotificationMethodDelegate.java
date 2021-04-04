package org.mockobor.listener_detectors;

import lombok.NonNull;
import lombok.Value;
import org.mockobor.mockedobservable.ListenersNotifier;

import java.lang.reflect.Method;


/**
 * Describe delegation from notification method to custom implementation.
 * <p>
 * {@link ListenersNotifier} is as context provided.
 */
@Value
public class NotificationMethodDelegate {

	Method source;

	NotificationMethodInvocation destination;


	/**
	 * Specified destination as function with arguments passed to invoked method.
	 */
	@FunctionalInterface
	public interface NotificationMethodInvocation {

		/**
		 * To invoke delegation method.
		 *
		 * @param listenersNotifier invocation destination (container with registered listeners and notification methods)
		 * @param method            invoked method
		 * @param arguments         arguments passed to invoked method
		 * @return result of invocation
		 */
		Object invoke( @NonNull ListenersNotifier listenersNotifier, @NonNull Method method, @NonNull Object... arguments );
	}
}

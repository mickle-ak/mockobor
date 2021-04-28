package org.mockobor.listener_detectors;

import lombok.NonNull;
import lombok.Value;
import org.mockobor.mockedobservable.ListenersNotifier;

import java.lang.reflect.Method;


/**
 * Describe delegation from notification method to custom implementation.
 * <p>
 * {@link ListenersNotifier} provided as context.
 */
@Value
public class NotificationMethodDelegate {

	/** Method of notifier object - interface methods, which should be redirected to implementation method. */
	@NonNull Method source;

	/** Implementation method as a function. */
	@NonNull NotificationMethodInvocation destination;


	/**
	 * Specified destination as a function with arguments passed to invoked method.
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

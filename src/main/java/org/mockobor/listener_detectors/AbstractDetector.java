package org.mockobor.listener_detectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.ListenerDefinition.ListenerDefinitionImpl;
import org.mockobor.listener_detectors.RegistrationDelegate.RegistrationInvocation;
import org.mockobor.utils.reflection.TypeUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.IntStream;


/**
 * The abstract detector class used to help by implementation of {@code ListenerDefinitionDetector}.
 */
public abstract class AbstractDetector implements ListenerDefinitionDetector {

	/**
	 * To check if the specified parameter type is a listener type.
	 * <p>
	 * For example: {@code Observer} or {@code PropertyChangeListener} are listener types.
	 *
	 * @param parameterType type to check
	 * @param method        method which contains this parameter (as context)
	 * @return true if the specified parameter type is a listener type; false otherwise
	 */
	protected abstract boolean isListenerClass( @NonNull Class<?> parameterType, @NonNull Method method );


	/**
	 * To check if the specified method is an add-method.
	 * <p>
	 * For example: {@code addListener}, {@code addObserver}, {@code addMyListener} etc.
	 *
	 * @param method method to check (it have at least one listener parameter, see {@link #isListenerClass(Class, Method)})
	 * @return true if the method is a registration method
	 */
	protected abstract boolean isAddMethods( @NonNull Method method );

	/**
	 * To check if the specified method is a remove-method.
	 * <p>
	 * For example: {@code removeListener}, {@code deleteObserver}, {@code removeMyListener} etc.
	 *
	 * @param method method to check (it have at least one listener parameter, see {@link #isListenerClass(Class, Method)})
	 * @return true if the method is a deregistration method
	 */
	protected abstract boolean isRemoveMethods( @NonNull Method method );


	/**
	 * To get a list of delegations to custom implementation of notification methods.
	 * <p></p>
	 * Notification delegates defined here take precedence over all other implementations of source method.
	 * <p></p>
	 * It returns an empty list per default.
	 *
	 * @return list of desirable notification delegates or empty list if no notification delegates needed
	 * @see ListenerDefinition#getCustomNotificationMethodDelegates()
	 */
	protected @NonNull List<NotificationMethodDelegate> getCustomNotificationMethodDelegates() {
		return Collections.emptyList();
	}


	/**
	 * To return additional interfaces which should be implemented by notifier object,
	 * returned from {@code createNotifierFor}.
	 * <p></p>
	 * Default method implemented in additional interfaces takes precedence over other implementations of same method
	 * (exclude defined in {@link #getCustomNotificationMethodDelegates()}).
	 * <p>
	 * Implementation for non-default methods of additional interfaces must be provided in {@link #getCustomNotificationMethodDelegates()}.
	 * <p></p>
	 * It returns an empty list per default.
	 *
	 * @return list of desirable additional interfaces or empty list if no additional interface needed
	 * @see ListenerDefinition#getAdditionalInterfaces()
	 */
	protected @NonNull List<Class<?>> getAdditionalInterfaces() {
		return Collections.emptyList();
	}


	// ==================================================================================
	// =================================== detect =======================================
	// ==================================================================================

	@Override
	public @NonNull ListenerDefinition detect( @NonNull Collection<Method> methods ) {
		ListenerDefinitionImpl listenerDefinition = detectRegistrations( methods );
		if( listenerDefinition.hasListenerDetected() ) {
			listenerDefinition.addAdditionalInterfaces( getAdditionalInterfaces() );
			getCustomNotificationMethodDelegates().forEach( listenerDefinition::addNotification );
		}
		return listenerDefinition;
	}


	// ==================================================================================
	// ================================ registration ====================================
	// ==================================================================================

	/**
	 * To detect registration methods (add/removeListener).
	 *
	 * @param methods methods of observable object
	 * @return listenersDefinition with detected listeners
	 * @see ListenerDefinition#hasListenerDetected()
	 */
	protected @NonNull ListenerDefinitionImpl detectRegistrations( @NonNull Collection<Method> methods ) {
		ListenerDefinitionImpl listenerDefinition = new ListenerDefinitionImpl();
		for( Method method : methods ) {
			ListenerRegistrationParameters registrationParameters = getListenerRegistrationParameter( method );
			if( registrationParameters != null ) {
				if( isAddMethods( method ) ) {
					RegistrationInvocation addDelegate = createAddDelegate( registrationParameters );
					listenerDefinition.addRegistration( new RegistrationDelegate( method, addDelegate ) );
					listenerDefinition.addDetectedListeners( registrationParameters.getListenerClasses() );
				} else if( isRemoveMethods( method ) ) {
					RegistrationInvocation removeDelegate = createRemoveDelegate( registrationParameters );
					listenerDefinition.addRegistration( new RegistrationDelegate( method, removeDelegate ) );
				}
			}
		}
		return listenerDefinition;
	}

	protected @NonNull RegistrationInvocation createAddDelegate( ListenerRegistrationParameters rp ) {
		return ( listeners, method, arguments ) -> createDelegate( rp, method, arguments, listeners::addListener );
	}

	protected @NonNull RegistrationInvocation createRemoveDelegate( ListenerRegistrationParameters rp ) {
		return ( listeners, method, arguments ) -> createDelegate( rp, method, arguments, listeners::removeListener );
	}

	@SuppressWarnings( "unchecked" )
	private <L> @Nullable Object createDelegate( @NonNull ListenerRegistrationParameters rp,
	                                             @NonNull Method method,
	                                             Object[] arguments,
	                                             @NonNull ListenerRegistration registration ) {
		if( arguments == null ) arguments = new Object[0];
		ListenerSelector selector = rp.createSelector( method, arguments );
		List<Integer> listenerIndexes = rp.getListenerIndexes();
		List<Class<?>> listenerClasses = rp.getListenerClasses();
		for( int i = 0; i < listenerIndexes.size(); i++ ) {
			Class<L> listenerClass = (Class<L>) listenerClasses.get( i );
			L listener = listenerClass.cast( arguments[listenerIndexes.get( i )] );
			registration.invoke( selector, listenerClass, listener );
		}
		return TypeUtils.getDefaultReturnValue( method.getReturnType() );
	}

	@FunctionalInterface
	private interface ListenerRegistration {

		<L> void invoke( @NonNull ListenerSelector selector, @NonNull Class<L> listenerClass, @NonNull L listener );
	}


	/**
	 * @param method method to check
	 * @return registration parameters if the specified method has one listener parameter; null otherwise
	 * @see #isListenerClass
	 */
	protected @Nullable ListenerRegistrationParameters getListenerRegistrationParameter( @NonNull Method method ) {
		List<Integer> selectorIndexes = new ArrayList<>();
		List<Integer> listenerIndexes = new ArrayList<>();
		List<Class<?>> listenerClasses = new ArrayList<>();
		Class<?>[] parameterTypes = method.getParameterTypes();
		for( int i = 0; i < parameterTypes.length; i++ ) {
			if( parameterTypes[i].isInterface() && isListenerClass( parameterTypes[i], method ) ) {
				listenerIndexes.add( i );
				listenerClasses.add( parameterTypes[i] );
			} else {
				selectorIndexes.add( i );
			}
		}
		return !listenerIndexes.isEmpty()
		       ? new ListenerRegistrationParameters( method, listenerIndexes, listenerClasses, selectorIndexes )
		       : null;
	}


	// ==================================================================================
	// =========================== RegistrationParameters ===============================
	// ==================================================================================

	/**
	 * To store information about listener and other parameters obtained from registration method.
	 */
	@RequiredArgsConstructor
	@Getter
	public static class ListenerRegistrationParameters {

		@Getter( AccessLevel.NONE )
		@NonNull
		private final Method registrationMethod; // only to compare with invoked method

		/** Index of listener parameter in registration method's arguments. */
		private final List<Integer> listenerIndexes;

		/** Type of listener found in registration method. */
		@NonNull
		private final List<Class<?>> listenerClasses;

		/** Indexes of other parameters (except listener) in registration method's arguments used to create selector. */
		@NonNull
		private final List<Integer> selectorIndexes;


		/**
		 * To create listener selector which describes actual invocation of registration method.
		 *
		 * @param invokedMethod invoked method to compare with definition method
		 * @param arguments     actual parameters used by real call of registration method
		 * @return listener selector used in this invocation of registration method
		 */
		public @NonNull ListenerSelector createSelector( @NonNull Method invokedMethod, @NonNull Object[] arguments ) {
			checkIsSameMethod( invokedMethod, arguments );
			return createSelector( invokedMethod, arguments, new ArrayList<>( selectorIndexes ) );
		}


		private static @NonNull ListenerSelector createSelector( @NonNull Method invokedMethod,
		                                                         @NonNull Object[] arguments,
		                                                         @NonNull List<Integer> selectorIndexesCopy ) {
			if( invokedMethod.isVarArgs() ) {
				if( arguments.length < invokedMethod.getParameterCount() ) {
					// vararg omitted => remove vararg elements from selector indexes
					selectorIndexesCopy.removeIf( idx -> idx >= arguments.length );
				} else if( arguments.length > invokedMethod.getParameterCount() ) {
					// more than one vararg => add indexes of extra parameters
					IntStream.range( invokedMethod.getParameterCount(), arguments.length ).forEach( selectorIndexesCopy::add );
				}
			}
			Object[] selectorArguments = new Object[selectorIndexesCopy.size()];
			for( int i = 0; i < selectorArguments.length; i++ ) {
				selectorArguments[i] = arguments[selectorIndexesCopy.get( i )];
			}
			return ListenerSelector.selector( selectorArguments );
		}


		private void checkIsSameMethod( @NonNull Method invokedMethod, @NonNull Object[] arguments ) {
			if( !Objects.equals( invokedMethod, registrationMethod ) ) {
				throw new MockoborImplementationError( "create selector for unexpected method (expected: %s, was: %s)",
				                                       registrationMethod, invokedMethod.getName() );
			}
			int declaredNumberOfArguments = selectorIndexes.size() + listenerIndexes.size();
			if( !invokedMethod.isVarArgs() ) {
				if( arguments.length != declaredNumberOfArguments ) {
					throw new MockoborImplementationError( "create selector for unexpected number of parameters (method: %s, expected: %d, was: %d:%s)",
					                                       invokedMethod.getName(), declaredNumberOfArguments,
					                                       arguments.length, Arrays.asList( arguments ) );
				}
			} else if( arguments.length < declaredNumberOfArguments - 1 ) { // less than with empty varargs
				throw new MockoborImplementationError( "create selector for unexpected number of parameters (vararg method: %s, expected>=%d, was: %d:%s)",
				                                       invokedMethod.getName(), declaredNumberOfArguments - 1,
				                                       arguments.length, Arrays.asList( arguments ) );
			}
		}
	}

}

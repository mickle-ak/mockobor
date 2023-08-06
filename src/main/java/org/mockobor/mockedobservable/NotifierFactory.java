package org.mockobor.mockedobservable;

import lombok.RequiredArgsConstructor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.mockobor.Mockobor;
import org.mockobor.exceptions.ListenerRegistrationMethodsNotDetectedException;
import org.mockobor.exceptions.MethodNotFoundException;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.exceptions.UnregisteredListenersFoundException;
import org.mockobor.listener_detectors.*;
import org.mockobor.listener_detectors.NotificationMethodDelegate.NotificationMethodInvocation;
import org.mockobor.listener_detectors.RegistrationDelegate.RegistrationInvocation;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler.Invocation;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistry;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableCollection;
import static org.mockobor.utils.reflection.ReflectionUtils.findSimilarMethod;
import static org.mockobor.utils.reflection.ReflectionUtils.getReachableMethods;


/**
 * Factory for notifier objects.
 * <p></p>
 * This method {@link #create} does follows:<ul>
 * <li>search for registration (add/remove listener) methods for all (known) observer/listeners,</li>
 * <li>detect used mocking tool,</li>
 * <li>redirect add/remove-listeners methods from mocked object to itself (using detected mocking tool) and</li>
 * <li>creates dynamic proxy as notifier object.</li>
 * </ul>
 * <p></p>
 * It used in {@link Mockobor#createNotifierFor} to do real work.
 */
@RequiredArgsConstructor
public class NotifierFactory {

	@NonNull
	private final ListenerDetectorsRegistry listenerDetectorsRegistry;

	@NonNull
	private final MockingToolsRegistry mockingToolsRegistry;


	/**
	 * To create a notifier object for the specified mocked observable.
	 *
	 * @param mockedObservable mock of observable object
	 * @param settings         settings used to create a new listener notifier
	 * @return notifier used to simulate notification calls from the specified mocked observable
	 * @throws ListenerRegistrationMethodsNotDetectedException if neither of listener definition detectors can detect listener registration methods
	 * @throws MockingToolNotDetectedException                 if the specified object not a mock or mocking tool used to mock it not supported
	 */
	public @NonNull ListenersNotifier create( @NonNull Object mockedObservable, @NonNull NotifierSettings settings )
			throws ListenerRegistrationMethodsNotDetectedException, MockingToolNotDetectedException {

		List<ListenerDefinition> listenerDefinitions = detectListenerDefinitions( mockedObservable );
		ListenersManager listenerManager = new ListenersManager( mockedObservable );
		listenerManager.setStrictCheckListenerList( settings.getStrictCheckListenerList() );
		registerInMockedObservable( listenerManager, listenerDefinitions );
		return createProxy( listenerManager, listenerDefinitions, settings );
	}


	/**
	 * To check if all listeners by all specified notifiers are unregistered.
	 *
	 * @param notifiers notifiers to check
	 * @throws UnregisteredListenersFoundException if some of the specified notifier contains unregistered listener(s)
	 */
	public void assertThatAllListenersAreUnregistered( @NonNull ListenersNotifier... notifiers )
			throws UnregisteredListenersFoundException {
		List<ListenersNotifier> notClean = Stream.of( notifiers )
		                                         .filter( n -> !n.allListenersAreUnregistered() )
		                                         .collect( Collectors.toList() );
		if( !notClean.isEmpty() ) {
			StringBuilder builder = new StringBuilder( "Found not unregistered listeners by:" );
			notClean.forEach( n -> {
				builder.append( "\n\t" ).append( n.getObservableMock() );
				n.getListenersWithSelector().forEach( lk -> builder.append( "\n\t\t" ).append( lk.toString() ) );
			} );
			throw new UnregisteredListenersFoundException( builder.toString() );
		}
	}


	// ==================================================================================
	// ============================== detect listeners ==================================
	// ==================================================================================

	/** To find all possible listener registrations in the specified mocked observable. */
	private @NonNull List<ListenerDefinition> detectListenerDefinitions( @NonNull Object mockedObservable ) {
		List<ListenerDefinition> listenerDefinitions = new ArrayList<>();
		Collection<Method> methods = getReachableMethods( mockedObservable );
		Collection<ListenerDefinitionDetector> detectors = listenerDetectorsRegistry.getDetectors();
		detectors.forEach( detector -> {
			ListenerDefinition definition = detector.detect( unmodifiableCollection( methods ) );
			if( definition.hasListenerDetected() ) {
				listenerDefinitions.add( definition );
				// remove processed registration methods
				definition.getRegistrations().forEach( delegation -> methods.remove( delegation.getSource() ) );
			}
		} );
		if( listenerDefinitions.isEmpty() ) throw new ListenerRegistrationMethodsNotDetectedException( mockedObservable, detectors );
		return listenerDefinitions;
	}


	// ==================================================================================
	// ============================== register in mock ==================================
	// ==================================================================================

	/** Redirect all found registration methods (add/remove listener methods) from mocked observable to listener container. */
	private void registerInMockedObservable( @NonNull ListenerContainer listenerManager,
	                                         @NonNull List<ListenerDefinition> listenerDefinitions ) {
		Object mockedObservable = listenerManager.getObservableMock();
		ListenerRegistrationHandler registrationHandler = mockingToolsRegistry.findHandlerForMock( mockedObservable );
		interceptPreviouslyListenerRegistrations( mockedObservable, registrationHandler, listenerManager, listenerDefinitions );
		redirectRegistrationMethods( registrationHandler, listenerManager, listenerDefinitions );
	}

	private void interceptPreviouslyListenerRegistrations( @NonNull Object observableMock,
	                                                       @NonNull ListenerRegistrationHandler registrationHandler,
	                                                       @NonNull ListenerContainer listenerManager,
	                                                       @NonNull List<ListenerDefinition> listenerDefinitions ) {

		Collection<Invocation> previouslyInvocations = registrationHandler.getPreviouslyRegistrations( observableMock );

		if( !previouslyInvocations.isEmpty() ) {
			Map<Method, RegistrationInvocation> registrationMethods =
					registrationDelegateStream( listenerDefinitions ).collect(
							Collectors.toMap( RegistrationDelegate::getSource, RegistrationDelegate::getDestination ) );
			previouslyInvocations.forEach( invocation -> {
				Method invokedMethod = invocation.getInvokedMethod();
				Method declaredMethod = findSimilarMethod( registrationMethods.keySet(), invokedMethod );
				if( declaredMethod != null ) {
					RegistrationInvocation registration = registrationMethods.get( declaredMethod ); // !=null because declaredMethod in keySet
					registration.invoke( listenerManager, declaredMethod, invocation.getArguments() );
				}
			} );
		}
	}

	private void redirectRegistrationMethods( @NonNull ListenerRegistrationHandler registrationHandler,
	                                          @NonNull ListenerContainer listenerManager,
	                                          @NonNull List<ListenerDefinition> listenerDefinitions ) {
		registrationDelegateStream( listenerDefinitions ).forEach(
				delegation -> registrationHandler.registerInMock( listenerManager, delegation ) );
	}

	private @NonNull Stream<RegistrationDelegate> registrationDelegateStream( @NonNull List<ListenerDefinition> listenerDefinitions ) {
		return listenerDefinitions.stream().flatMap( definition -> definition.getRegistrations().stream() );
	}


	// ==================================================================================
	// ================================ create proxy ====================================
	// ==================================================================================

	private @NonNull ListenersNotifier createProxy( @NonNull ListenersNotifier listenersNotifier,
	                                                @NonNull List<ListenerDefinition> listenerDefinitions,
	                                                @NonNull NotifierSettings settings ) {
		// collect interfaces to implement
		Set<Class<?>> additionalInterfaces = collectAdditionalInterfaces( listenerDefinitions );
		Set<Class<?>> detectedListenerToImplement = collectDetectedListenerToImplement( listenerDefinitions, settings );
		Set<Class<?>> interfacesToImplement = new LinkedHashSet<>( additionalInterfaces );
		interfacesToImplement.add( ListenersNotifier.class ); // ListenersNotifier must always be implemented
		interfacesToImplement.addAll( detectedListenerToImplement );

		// create invocation handler for proxy
		InvocationHandler invocationHandler = createInvocationHandler(
				listenersNotifier,
				collectCustomNotificationDelegates( listenerDefinitions ),
				additionalInterfaces,
				detectedListenerToImplement );

		// create proxy, which implement required interfaces
		return (ListenersNotifier) Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(),
		                                                   interfacesToImplement.toArray( new Class<?>[0] ),
		                                                   invocationHandler );
	}

	@SuppressWarnings( "java:S3776" ) // sonarlint, the methods is not too complex.
	private static @NonNull InvocationHandler createInvocationHandler(
			@NonNull ListenersNotifier listenersNotifier,
			@NonNull Map<Method, NotificationMethodInvocation> customNotificationDelegates,
			@NonNull Set<Class<?>> additionalInterfaces,
			@NonNull Set<Class<?>> detectedListenerToImplement ) {

		return ( proxy, method, args ) -> {

			try {
				Class<?> declaringClass = method.getDeclaringClass();

				// 1. try in explicit notification invocations
				NotificationMethodInvocation delegate = findDelegate( customNotificationDelegates, method );
				if( delegate != null ) {
					return delegate.invoke( listenersNotifier, method, args );
				}

				// 2. tries as default method of additional interface
				if( method.isDefault() && additionalInterfaces.contains( declaringClass ) ) {
					return ReflectionUtils.invokeDefaultMethod( proxy, method, args );
				}

				// 3. try ListenersNotifier
				if( declaringClass.equals( ListenersNotifier.class ) || declaringClass.equals( Object.class ) ) {
					return method.invoke( listenersNotifier, args );
				}

				// 4. try similar methods declared in listenersNotifier
				Method compatibleMethod = findSimilarMethod( getReachableMethods( listenersNotifier ), method );
				if( compatibleMethod != null ) {
					return compatibleMethod.invoke( listenersNotifier, args );
				}

				// 5. try to invoke detected listener's method over ListenersNotifier.notifierFor
				if( detectedListenerToImplement.contains( declaringClass ) ) {
					Object listenerProxy = listenersNotifier.notifierFor( declaringClass );
					return method.invoke( listenerProxy, args );
				}

				// can not find an invocation handler for the method
				throw new MethodNotFoundException( method, args );
			}
			catch( InvocationTargetException e ) {
				// this is needed to throw the original exception to the caller.
				Throwable cause = e.getCause();
				throw cause != null ? cause : e;
			}
		};
	}

	private static @Nullable NotificationMethodInvocation findDelegate( @NonNull Map<Method, NotificationMethodInvocation> delegates,
	                                                                    @NonNull Method invokedMethod ) {
		Method delegateSourceMethod = findSimilarMethod( delegates.keySet(), invokedMethod );
		return delegateSourceMethod != null ? delegates.get( delegateSourceMethod ) : null;
	}


	private @NonNull Set<Class<?>> collectAdditionalInterfaces( @NonNull List<ListenerDefinition> listenerDefinitions ) {
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		listenerDefinitions.forEach( ld -> interfaces.addAll( ld.getAdditionalInterfaces() ) );
		return interfaces;
	}

	private @NonNull Set<Class<?>> collectDetectedListenerToImplement( @NonNull List<ListenerDefinition> listenerDefinitions,
	                                                                   @NonNull NotifierSettings settings ) {
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		if( settings.shouldNotifierImplementListenerInterfaces() ) {
			listenerDefinitions.forEach( ld -> interfaces.addAll( ld.getDetectedListeners() ) );
		}
		return interfaces;
	}

	private @NonNull Map<Method, NotificationMethodInvocation> collectCustomNotificationDelegates(
			@NonNull List<ListenerDefinition> listenerDefinitions ) {
		Map<Method, NotificationMethodInvocation> customNotificationDelegates = new HashMap<>();
		listenerDefinitions.forEach( ld -> customNotificationDelegates.putAll( ld.getCustomNotificationMethodDelegates() ) );
		return customNotificationDelegates;
	}

}

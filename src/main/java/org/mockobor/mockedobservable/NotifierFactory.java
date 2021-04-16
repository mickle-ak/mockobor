package org.mockobor.mockedobservable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.mockobor.Mockobor;
import org.mockobor.exceptions.ListenerRegistrationMethodsNotDetectedException;
import org.mockobor.exceptions.MethodNotFoundException;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.ListenerDefinitionDetector;
import org.mockobor.listener_detectors.ListenerDetectorsRegistry;
import org.mockobor.listener_detectors.ListenersDefinition;
import org.mockobor.listener_detectors.NotificationMethodDelegate.NotificationMethodInvocation;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistry;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static org.mockobor.utils.reflection.ReflectionUtils.findCompatibleMethod;
import static org.mockobor.utils.reflection.ReflectionUtils.getReachableMethods;


/**
 * Factory for notifier objects.
 * <p></p>
 * It searchs for registration (add/remove listener) methods for all possible observer/listeners,
 * detect used mocking tool,
 * redirect add/remove-listeners methods from mocked object to itself (using detected mocking tool) and
 * creates dynamic proxy as notifier object.
 * <p></p>
 * It used in {@link Mockobor#createNotifierFor} to do real work.
 */
@RequiredArgsConstructor
public class NotifierFactory {

	@NonNull
	private final ListenerDetectorsRegistry listenerDetectorsRegistry;

	@NonNull
	private final MockingToolsRegistry mockingToolsRegistry;

	/** Flag: should notifier object implements detected listeners. */
	@SuppressWarnings( { "FieldCanBeLocal", "FieldMayBeFinal" } )
	@Setter
	private boolean implementListeners = true;


	/**
	 * To create notifier object for the specified mocked observable.
	 *
	 * @param mockedObservable mock of observable object
	 * @return notifier used to simulate notification calls from the specified mocked observable
	 * @throws ListenerRegistrationMethodsNotDetectedException if neither of listener definition detectors can detect listener registration methods
	 * @throws MockingToolNotDetectedException                 if the specified object not a mock or mocking tool used to mock it not supported
	 */
	@NonNull
	public ListenersNotifier create( @NonNull Object mockedObservable )
			throws ListenerRegistrationMethodsNotDetectedException, MockingToolNotDetectedException {
		List<ListenersDefinition> listenersDefinitions = detectListenersDefinitions( mockedObservable );
		ListenersManager listenerManager = new ListenersManager( mockedObservable );
		registerInMockedObservable( listenerManager, listenersDefinitions );
		return createProxy( listenerManager, listenersDefinitions );
	}


	// ==================================================================================
	// ============================== detect listeners ==================================
	// ==================================================================================

	/** To find all possible listener registration in the specified mocked observable. */
	private List<ListenersDefinition> detectListenersDefinitions( @NonNull Object mockedObservable ) {
		List<ListenersDefinition> listenersDefinitions = new ArrayList<>();
		Collection<Method> methods = getReachableMethods( mockedObservable );
		Collection<ListenerDefinitionDetector> detectors = listenerDetectorsRegistry.getDetectors();
		detectors.forEach( detector -> {
			ListenersDefinition definition = detector.detect( unmodifiableCollection( methods ) );
			if( definition.hasListenerDetected() ) {
				listenersDefinitions.add( definition );
				// remove processed registration  methods
				definition.getRegistrations().forEach( delegation -> methods.remove( delegation.getSource() ) );
			}
		} );
		if( listenersDefinitions.isEmpty() ) throw new ListenerRegistrationMethodsNotDetectedException( mockedObservable, detectors );
		return listenersDefinitions;
	}


	// ==================================================================================
	// ============================== register in mock ==================================
	// ==================================================================================

	/** Redirect all found registration methods (add/remove listener methods) from mocked observable to listener container. */
	private void registerInMockedObservable( @NonNull ListenerContainer listenerManager,
	                                         @NonNull List<ListenersDefinition> listenersDefinitions ) {
		Object mockedObservable = listenerManager.getObservableMock();
		ListenerRegistrationHandler registrationHandler = mockingToolsRegistry.findHandlerForMock( mockedObservable );
		listenersDefinitions
				.stream()
				.flatMap( definition -> definition.getRegistrations().stream() )
				.forEach( delegation -> registrationHandler.registerInMock( listenerManager, delegation ) );
	}


	// ==================================================================================
	// ================================ create proxy ====================================
	// ==================================================================================

	private ListenersNotifier createProxy( @NonNull ListenersNotifier listenersNotifier,
	                                       @NonNull List<ListenersDefinition> listenersDefinitions ) {
		// collect interfaces to implement
		Set<Class<?>> additionalInterfaces = collectAdditionalInterfaces( listenersDefinitions );
		Set<Class<?>> detectedListenerToImplement = collectDetectedListenerToImplement( listenersDefinitions );
		Set<Class<?>> interfacesToImplement = new LinkedHashSet<>( additionalInterfaces );
		interfacesToImplement.add( ListenersNotifier.class ); // ListenersNotifier must be always implemented
		interfacesToImplement.addAll( detectedListenerToImplement );

		// create invocation handler for proxy
		InvocationHandler invocationHandler = createInvocationHandler(
				listenersNotifier,
				collectCustomNotificationDelegates( listenersDefinitions ),
				additionalInterfaces,
				detectedListenerToImplement );

		// create proxy, which implement required interfaces
		return (ListenersNotifier) Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(),
		                                                   interfacesToImplement.toArray( new Class<?>[0] ),
		                                                   invocationHandler );
	}

	@SuppressWarnings( "java:S3776" ) // sonarlint, the methods is not too complex.
	private static InvocationHandler createInvocationHandler(
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

				// 2. try as default method of additional interface
				if( method.isDefault() && additionalInterfaces.contains( declaringClass ) ) {
					return ReflectionUtils.invokeDefaultMethod( proxy, method, args );
				}

				// 3. try ListenersNotifier
				if( declaringClass.equals( ListenersNotifier.class ) || declaringClass.equals( Object.class ) ) {
					return method.invoke( listenersNotifier, args );
				}

				// 4. try compatible methods declared in listenersNotifier
				Method compatibleMethod = findCompatibleMethod( getReachableMethods( listenersNotifier ), method );
				if( compatibleMethod != null ) {
					return compatibleMethod.invoke( listenersNotifier, args );
				}

				// 5. try to invoke detected listener's method over ListenersNotifier.notifierFor
				if( detectedListenerToImplement.contains( declaringClass ) ) {
					Object listenerProxy = listenersNotifier.notifierFor( declaringClass );
					return method.invoke( listenerProxy, args );
				}

				// can not find invocation handle for the method
				throw new MethodNotFoundException( method, args );
			}
			catch( InvocationTargetException e ) {
				// this is needed to throw the original exception to the caller.
				Throwable cause = e.getCause();
				throw cause != null ? cause : e;
			}
		};
	}

	private static NotificationMethodInvocation findDelegate( @NonNull Map<Method, NotificationMethodInvocation> delegates,
	                                                          @NonNull Method invokedMethod ) {
		Method delegateSourceMethod = findCompatibleMethod( delegates.keySet(), invokedMethod );
		return delegateSourceMethod != null ? delegates.get( delegateSourceMethod ) : null;
	}


	private @NonNull Set<Class<?>> collectAdditionalInterfaces( @NonNull List<ListenersDefinition> listenersDefinitions ) {
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		listenersDefinitions.forEach( ld -> interfaces.addAll( ld.getAdditionalInterfaces() ) );
		return interfaces;
	}

	private @NonNull Set<Class<?>> collectDetectedListenerToImplement( @NonNull List<ListenersDefinition> listenersDefinitions ) {
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		if( implementListeners ) {
			listenersDefinitions.forEach( ld -> interfaces.addAll( ld.getDetectedListeners() ) );
		}
		return interfaces;
	}

	private @NonNull Map<Method, NotificationMethodInvocation> collectCustomNotificationDelegates(
			@NonNull List<ListenersDefinition> listenersDefinitions ) {
		Map<Method, NotificationMethodInvocation> customNotificationDelegates = new HashMap<>();
		listenersDefinitions.forEach( ld -> customNotificationDelegates.putAll( ld.getCustomNotificationMethodDelegates() ) );
		return customNotificationDelegates;
	}

}

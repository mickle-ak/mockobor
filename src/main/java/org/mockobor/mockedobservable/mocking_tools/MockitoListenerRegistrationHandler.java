package org.mockobor.mockedobservable.mocking_tools;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.mockito.stubbing.Answer;
import org.mockobor.exceptions.MockoborException;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;
import org.mockobor.listener_detectors.RegistrationDelegate.RegistrationInvocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.mockito.Mockito.lenient;


/**
 * Implementation of {@link ListenerRegistrationHandler} for Mockito.
 */
public class MockitoListenerRegistrationHandler implements ListenerRegistrationHandler {

	@Override
	public boolean canHandle( @Nullable Object mockedObservable ) {
		return MockUtil.isMock( mockedObservable );
	}

	// ==================================================================================
	// =============================== registerInMock ===================================
	// ==================================================================================

	@Override
	public void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registration ) {
		Object mockedObservable = listeners.getObservableMock();
		if( !MockUtil.isMock( mockedObservable ) ) throw new MockoborImplementationError( "observable (%s) must be a mockito mock", mockedObservable );

		try {
			Object stabbingObject = lenient().doAnswer( createAnswer( listeners, registration.getDestination() ) ).when( mockedObservable );
			Method sourceMethod = registration.getSource();
			sourceMethod.invoke( stabbingObject, createArgumentMatchers( sourceMethod ) );
		}
		catch( IllegalAccessException | InvocationTargetException e ) {
			throw new MockoborException( e );
		}
	}

	/** To create answer as redirection to the specified registration delegate. */
	private @NonNull Answer<Object> createAnswer( @NonNull ListenerContainer listeners, @NonNull RegistrationInvocation destination ) {
		return invocation -> destination.invoke( listeners, invocation.getMethod(), invocation.getArguments() );
	}

	/** To create argument matchers for stabbing invocation using {@link Mockito#any()} for all arguments. */
	private @NonNull Object[] createArgumentMatchers( @NonNull Method sourceMethod ) {
		Class<?>[] parameterTypes = sourceMethod.getParameterTypes();
		Object[] result = new Object[parameterTypes.length];
		for( int i = 0; i < result.length; i++ ) {
			result[i] = ANY_FOR_PRIMITIVE_TYPES.getOrDefault( parameterTypes[i], ArgumentMatchers::any ).get();
		}
		return result;
	}

	private static final Map<Class<?>, Supplier<Object>> ANY_FOR_PRIMITIVE_TYPES = new HashMap<>();

	static {
		ANY_FOR_PRIMITIVE_TYPES.put( byte.class, ArgumentMatchers::anyByte );
		ANY_FOR_PRIMITIVE_TYPES.put( short.class, ArgumentMatchers::anyShort );
		ANY_FOR_PRIMITIVE_TYPES.put( int.class, ArgumentMatchers::anyInt );
		ANY_FOR_PRIMITIVE_TYPES.put( long.class, ArgumentMatchers::anyLong );
		ANY_FOR_PRIMITIVE_TYPES.put( float.class, ArgumentMatchers::anyFloat );
		ANY_FOR_PRIMITIVE_TYPES.put( double.class, ArgumentMatchers::anyDouble );
		ANY_FOR_PRIMITIVE_TYPES.put( char.class, ArgumentMatchers::anyChar );
		ANY_FOR_PRIMITIVE_TYPES.put( boolean.class, ArgumentMatchers::anyBoolean );
	}

	// ==================================================================================
	// ========================= getPreviouslyRegistrations =============================
	// ==================================================================================

	@Override
	public @NonNull Collection<Invocation> getPreviouslyRegistrations( @NonNull Object mockedObservable ) {
		if( !MockUtil.isMock( mockedObservable ) ) throw new MockoborImplementationError( "observable (%s) must be a mockito mock", mockedObservable );

		return Mockito.mockingDetails( mockedObservable )
		              .getInvocations().stream()
		              .map( i -> new Invocation( i.getMethod(), i.getArguments() ) )
		              .collect( Collectors.toList() );
	}
}

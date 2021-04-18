package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.Mockito.doAnswer;


/**
 * Implementation of {@link ListenerRegistrationHandler} for Mockito.
 */
public class MockitoListenerRegistrationHandler implements ListenerRegistrationHandler {

	@Override
	public boolean canHandle( Object mockedObservable ) {
		return MockUtil.isMock( mockedObservable );
	}

	@Override
	public void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registration ) {
		Object mockedObject = listeners.getObservableMock();
		if( !MockUtil.isMock( mockedObject ) ) throw new MockoborImplementationError( "observable (%s) must be a mockito mock", mockedObject );

		try {
			Object stabbingObject = doAnswer( createAnswer( listeners, registration.getDestination() ) ).when( mockedObject );
			Method sourceMethod = registration.getSource();
			sourceMethod.invoke( stabbingObject, createArgumentMatchers( sourceMethod ) );
		}
		catch( IllegalAccessException | InvocationTargetException e ) {
			throw new MockoborException( e );
		}
	}

	/** To create answer as redirection to the specified registration delegate. */
	@NonNull
	private Answer<Object> createAnswer( @NonNull ListenerContainer listeners, @NonNull RegistrationInvocation destination ) {
		return invocation -> destination.invoke( listeners, invocation.getMethod(), invocation.getArguments() );
	}

	/** To create argument matchers for stabbing invocation using {@link Mockito#any()} for all arguments. */
	@NonNull
	private Object[] createArgumentMatchers( @NonNull Method sourceMethod ) {
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
}

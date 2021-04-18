package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.mockobor.exceptions.MockoborException;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;
import org.mockobor.listener_detectors.RegistrationDelegate.RegistrationInvocation;
import org.mockobor.utils.reflection.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Implementation of {@link ListenerRegistrationHandler} for EasyMock.
 */
public class EasymockListenerRegistrationHandler implements ListenerRegistrationHandler {

	@Override
	public boolean canHandle( Object mockedObservable ) {
		return mockedObservable != null
		       && ( mockedObservable.toString().startsWith( "EasyMock for " )
		            || ReflectionUtils.isEasymockMock( mockedObservable.getClass() ) );
	}

	@Override
	public void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registration ) {
		Object mockedObject = listeners.getObservableMock();
		if( !canHandle( mockedObject ) ) throw new MockoborImplementationError( "observable (%s) must be mocked by easymock", mockedObject );

		try {
			Method sourceMethod = registration.getSource();
			Object[] argumentMatchers = createArgumentMatchers( sourceMethod );

			sourceMethod.invoke( mockedObject, argumentMatchers );
			EasyMock.expectLastCall().andStubAnswer( createAnswer( sourceMethod, listeners, registration.getDestination() ) );
		}
		catch( IllegalAccessException | InvocationTargetException e ) {
			throw new MockoborException( e );
		}

	}

	/** To create answer as redirection to the specified registration delegate. */
	@NonNull
	private IAnswer<Object> createAnswer( @NonNull Method sourceMethod,
	                                      @NonNull ListenerContainer listeners,
	                                      @NonNull RegistrationInvocation destination ) {
		return () -> destination.invoke( listeners, sourceMethod, EasyMock.getCurrentArguments() );
	}

	/** To create argument matchers for stabbing invocation using {@link EasyMock#anyObject()} for all arguments. */
	@NonNull
	private Object[] createArgumentMatchers( @NonNull Method sourceMethod ) {
		Class<?>[] parameterTypes = sourceMethod.getParameterTypes();
		Object[] result = new Object[parameterTypes.length];
		for( int i = 0; i < result.length; i++ ) {
			result[i] = ANY_FOR_PRIMITIVE_TYPES.getOrDefault( parameterTypes[i], EasyMock::anyObject ).get();
		}
		return result;
	}

	private static final Map<Class<?>, Supplier<Object>> ANY_FOR_PRIMITIVE_TYPES = new HashMap<>();

	static {
		ANY_FOR_PRIMITIVE_TYPES.put( byte.class, EasyMock::anyByte );
		ANY_FOR_PRIMITIVE_TYPES.put( short.class, EasyMock::anyShort );
		ANY_FOR_PRIMITIVE_TYPES.put( int.class, EasyMock::anyInt );
		ANY_FOR_PRIMITIVE_TYPES.put( long.class, EasyMock::anyLong );
		ANY_FOR_PRIMITIVE_TYPES.put( float.class, EasyMock::anyFloat );
		ANY_FOR_PRIMITIVE_TYPES.put( double.class, EasyMock::anyDouble );
		ANY_FOR_PRIMITIVE_TYPES.put( char.class, EasyMock::anyChar );
		ANY_FOR_PRIMITIVE_TYPES.put( boolean.class, EasyMock::anyBoolean );
	}
}

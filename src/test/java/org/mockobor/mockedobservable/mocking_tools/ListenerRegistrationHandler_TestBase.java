package org.mockobor.mockedobservable.mocking_tools;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;
import org.mockobor.mockedobservable.ListenersManager;
import org.mockobor.utils.reflection.ReflectionUtils;
import org.mockobor.utils.reflection.TypeUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


abstract class ListenerRegistrationHandler_TestBase {

	private final TestMethods                 mock                   = createMock();
	private final ListenerRegistrationHandler handler                = createListenerRegistrationHandler();
	private final ListenerContainer           listeners              = new ListenersManager( mock );
	private final List<ListenerContainer>     usedListenerContainers = new ArrayList<>();
	private final List<Object>                invocationArguments    = new ArrayList<>();


	protected abstract TestMethods createMock();

	protected abstract ListenerRegistrationHandler createListenerRegistrationHandler();

	protected abstract Object createPartialMock( @NonNull Object object );

	protected abstract void endOfStubbingMode( Object mock );


	// ==================================================================================
	// ================================== canHandle =====================================
	// ==================================================================================

	@Test
	void canHandle() {
		assertThat( handler.canHandle( mock ) ).isTrue();
		assertThat( handler.canHandle( listeners ) ).isFalse();
		assertThat( handler.canHandle( null ) ).isFalse();
	}

	@Test
	void canHandle_partial_mock() {
		Assumptions.assumeFalse( this.getClass().getSimpleName().startsWith( "Easymock" ) && ReflectionUtils.javaSpecificationVersion() >= 17,
		                         "EasyMock and Java 17+" );

		assertThat( handler.canHandle( createPartialMock( listeners ) ) ).isTrue();
	}


	// ==================================================================================
	// ================================ registerInMock ==================================
	// ==================================================================================

	@Test
	void registerInMock_objectArguments() {

		handler.registerInMock( listeners, createDelegate( "objectArguments" ) );
		endOfStubbingMode( mock );

		mock.objectArguments( "p1", "p2", 123 );
		assertThat( invocationArguments ).containsExactly( "p1", "p2", 123 );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}

	@Test
	void registerInMock_stringArgument() {

		handler.registerInMock( listeners, createDelegate( "stringArgument" ) );
		endOfStubbingMode( mock );

		mock.stringArgument( "some value" );
		assertThat( invocationArguments ).containsExactly( "some value" );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}

	@Test
	void registerInMock_int2int() {

		handler.registerInMock( listeners, createDelegate( "int2int" ) );
		endOfStubbingMode( mock );

		int r = mock.int2int( 4567 );
		assertThat( invocationArguments ).containsExactly( 4567 );
		assertThat( r ).isZero();
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}

	@Test
	void registerInMock_returnType() {

		handler.registerInMock( listeners, createDelegate( "returnType" ) );
		endOfStubbingMode( mock );

		String result = mock.returnType();
		assertThat( invocationArguments ).isEmpty();
		assertThat( result ).isEmpty();
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}

	@Test
	void registerInMock_primitiveArguments() {

		handler.registerInMock( listeners, createDelegate( "primitiveArguments" ) );
		endOfStubbingMode( mock );

		mock.primitiveArguments( 1, 2L, (char) 3, (short) 5, (byte) 6, 7f, 8d, true, "string" );
		assertThat( invocationArguments ).containsExactly( 1, 2L, (char) 3, (short) 5, (byte) 6, 7f, 8d, true, "string" );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}

	@Test
	void registerInMock_varargsObject() {

		handler.registerInMock( listeners, createDelegate( "varargObject" ) );
		endOfStubbingMode( mock );

		mock.varargObject( "p1", 2f, "v1", 3L, 3, 'c' );
		assertThat( invocationArguments ).containsExactly( "p1", 2f, "v1", 3L, 3, 'c' );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}

	@Test
	void registerInMock_varargsInt() {

		handler.registerInMock( listeners, createDelegate( "varargInt" ) );
		endOfStubbingMode( mock );

		mock.varargInt( "p1", 3, 5, 8, 13, 21 );
		assertThat( invocationArguments ).containsExactly( "p1", 3, 5, 8, 13, 21 );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}

	@SuppressWarnings( "java:S5778" ) // suppress "Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception"
	@Test
	void registerInMock_notMock() {
		assertThatThrownBy( () -> handler.registerInMock( new ListenersManager( new Object() ), createDelegate( "returnType" ) ) )
				.isInstanceOf( MockoborImplementationError.class )
				.hasMessageContaining( "mock" );
	}


	// ==================================================================================
	// ================================= help methods ===================================
	// ==================================================================================

	private RegistrationDelegate createDelegate( String methodName ) {
		return new RegistrationDelegate( findMethod( methodName ), this::destinationMethod );
	}

	private Object destinationMethod( ListenerContainer listeners, Method sourceMethods, Object... arguments ) {
		usedListenerContainers.add( listeners );
		invocationArguments.addAll( Arrays.asList( arguments ) );
		return TypeUtils.getDefaultReturnValue( sourceMethods.getReturnType() );
	}

	@SuppressWarnings( "OptionalGetWithoutIsPresent" )
	private Method findMethod( String methodName ) {
		return Arrays.stream( mock.getClass().getDeclaredMethods() ).filter( m -> m.getName().equals( methodName ) ).findFirst().get();
	}
}

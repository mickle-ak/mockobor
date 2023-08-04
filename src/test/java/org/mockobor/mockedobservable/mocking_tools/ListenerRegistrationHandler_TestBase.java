package org.mockobor.mockedobservable.mocking_tools;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;
import org.mockobor.mockedobservable.ListenersManager;
import org.mockobor.utils.reflection.TypeUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


abstract class ListenerRegistrationHandler_TestBase {

	protected final TestMethods                 mock                   = createMock();
	protected final ListenerRegistrationHandler handler                = createListenerRegistrationHandler();
	protected final ListenerContainer           listeners              = new ListenersManager( mock );
	protected final List<ListenerContainer>     usedListenerContainers = new ArrayList<>();
	protected final List<Object>                invocationArguments    = new ArrayList<>();


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
		assertThat( handler.canHandle( createPartialMock( listeners ) ) ).isTrue();
	}


	// ==================================================================================
	// ================================ registerInMock ==================================
	// ==================================================================================

	@Test
	void registerInMock_objectArguments() {

		handler.registerInMock( listeners, createDelegate( "objectArguments" ) );
		endOfStubbingMode( mock );

		mock.objectArguments( "r1", null, 0 );
		mock.objectArguments( "p1", "p2", 123 );

		assertThat( invocationArguments ).containsExactly(
				Arrays.asList( "r1", null, 0 ),
				Arrays.asList( "p1", "p2", 123 ) );
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	@Test
	void registerInMock_stringArgument() {

		handler.registerInMock( listeners, createDelegate( "stringArgument" ) );
		endOfStubbingMode( mock );

		mock.stringArgument( "some value" );
		mock.stringArgument( null );

		assertThat( invocationArguments ).containsExactly(
				List.of( "some value" ),
				Collections.singletonList( null ) );
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	@Test
	void registerInMock_int2int() {

		handler.registerInMock( listeners, createDelegate( "int2int" ) );
		endOfStubbingMode( mock );

		int r1 = mock.int2int( 4567 );
		int r2 = mock.int2int( 9876 );

		assertThat( invocationArguments ).containsExactly(
				List.of( 4567 ),
				List.of( 9876 ) );
		assertThat( r1 ).isZero();
		assertThat( r2 ).isZero();
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	@Test
	void registerInMock_returnType() {

		handler.registerInMock( listeners, createDelegate( "returnType" ) );
		endOfStubbingMode( mock );

		String r1 = mock.returnType();
		String r2 = mock.returnType();

		assertThat( invocationArguments ).containsExactly( List.of(), List.of() );
		assertThat( r1 ).isEmpty();
		assertThat( r2 ).isEmpty();
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	@Test
	void registerInMock_primitiveArguments() {

		handler.registerInMock( listeners, createDelegate( "primitiveArguments" ) );
		endOfStubbingMode( mock );

		mock.primitiveArguments( 1, 2L, (char) 3, (short) 5, (byte) 6, 7f, 8d, true, null );
		mock.primitiveArguments( 10, 20L, (char) 30, (short) 50, (byte) 60, 70f, 80d, false, "string" );

		assertThat( invocationArguments ).containsExactly(
				Arrays.asList( 1, 2L, (char) 3, (short) 5, (byte) 6, 7f, 8d, true, null ),
				Arrays.asList( 10, 20L, (char) 30, (short) 50, (byte) 60, 70f, 80d, false, "string" ) );
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	@Test
	void registerInMock_arrayObject() {

		handler.registerInMock( listeners, createDelegate( "arrayObject" ) );
		endOfStubbingMode( mock );

		mock.arrayObject( "p1", new Object[]{ 2f, null, 3L, 3, 'c' } );
		mock.arrayObject( "p2", new Object[]{ 22f, "v1", 33L, 44, 'd' } );
		mock.arrayObject( "p3", new Object[0] );
		mock.arrayObject( null, null );

		assertThat( invocationArguments )
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactly(
						Arrays.asList( "p1", new Object[]{ 2f, null, 3L, 3, 'c' } ),
						Arrays.asList( "p2", new Object[]{ 22f, "v1", 33L, 44, 'd' } ),
						Arrays.asList( "p3", new Object[0] ),
						Arrays.asList( null, null ) );
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	@Test
	void registerInMock_arrayLong() {

		handler.registerInMock( listeners, createDelegate( "arrayLong" ) );
		endOfStubbingMode( mock );

		mock.arrayLong( null, new long[]{ 1 } );
		mock.arrayLong( "p2", new long[]{ 3, 5, 8, 13, 21 } );
		mock.arrayLong( "p3", new long[0] );
		mock.arrayLong( null, null );

		assertThat( invocationArguments )
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactly(
						Arrays.asList( null, new long[]{ 1 } ),
						Arrays.asList( "p2", new long[]{ 3, 5, 8, 13, 21 } ),
						Arrays.asList( "p3", new long[0] ),
						Arrays.asList( null, null ) );
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	// suppress "Refactor the code of the lambda to have only one invocation, possibly throwing a runtime exception"
	@SuppressWarnings( "java:S5778" )
	@Test
	void registerInMock_notMock() {
		assertThatThrownBy( () -> handler.registerInMock( new ListenersManager( new Object() ), createDelegate( "returnType" ) ) )
				.isInstanceOf( MockoborImplementationError.class )
				.hasMessageContaining( "mock" );
	}


	// ==================================================================================
	// ================================= help methods ===================================
	// ==================================================================================

	protected RegistrationDelegate createDelegate( String methodName ) {
		return new RegistrationDelegate( findMethod( methodName ), this::destinationMethod );
	}

	private Object destinationMethod( ListenerContainer listeners, Method sourceMethods, Object... arguments ) {
		usedListenerContainers.add( listeners );
		invocationArguments.add( Arrays.asList( arguments ) );
		return TypeUtils.getDefaultReturnValue( sourceMethods.getReturnType() );
	}

	@SuppressWarnings( "OptionalGetWithoutIsPresent" )
	private Method findMethod( String methodName ) {
		return Arrays.stream( mock.getClass().getDeclaredMethods() ).filter( m -> m.getName().equals( methodName ) ).findFirst().get();
	}
}

package org.mockobor.mockedobservable.mocking_tools;

import org.junit.jupiter.api.Test;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;
import org.mockobor.mockedobservable.ListenersManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


@SuppressWarnings( "OptionalGetWithoutIsPresent" )
class MockitoListenerRegistrationHandlerTest {

	private interface TestMethods {

		void objectArguments( Object a1, Object a2, Object a3 );

		void stringArgument( String s );

		int int2int( Integer i );

		String returnType();

		void primitiveArguments( int i, long l, char c, short s, byte b, float f, double d, boolean bn, String st );

		void varargObject( Object... params );

		void varargInt( int... params );
	}

	private final TestMethods mock = mock( TestMethods.class );

	private final MockitoListenerRegistrationHandler mockitoHandler = new MockitoListenerRegistrationHandler();

	private final ListenerContainer listeners = new ListenersManager( mock );

	private final List<ListenerContainer> usedListenerContainers = new ArrayList<>();
	private final List<Object>            invocationArguments    = new ArrayList<>();


	// ==================================================================================
	// ================================== canHandle =====================================
	// ==================================================================================

	@Test
	void canHandle() {
		assertThat( mockitoHandler.canHandle( mock ) ).isTrue();
		assertThat( mockitoHandler.canHandle( listeners ) ).isFalse();
		assertThat( mockitoHandler.canHandle( spy( listeners ) ) ).isTrue();
	}


	// ==================================================================================
	// ================================ registerInMock ==================================
	// ==================================================================================

	@Test
	void registerInMock_objectArguments() {

		mockitoHandler.registerInMock( listeners, createDelegate( "objectArguments" ) );

		mock.objectArguments( "p1", "p2", 123 );
		assertThat( invocationArguments ).containsExactly( "p1", "p2", 123 );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}


	@Test
	void registerInMock_stringArgument() {

		mockitoHandler.registerInMock( listeners, createDelegate( "stringArgument" ) );

		mock.stringArgument( "some value" );
		assertThat( invocationArguments ).containsExactly( "some value" );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}


	@Test
	void registerInMock_int2int() {

		mockitoHandler.registerInMock( listeners, createDelegate( "int2int" ) );

		int r = mock.int2int( 4567 );
		assertThat( invocationArguments ).containsExactly( 4567 );
		assertThat( r ).isZero();
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}


	@Test
	void registerInMock_returnType() {

		mockitoHandler.registerInMock( listeners, createDelegate( "returnType" ) );

		String result = mock.returnType();
		assertThat( invocationArguments ).isEmpty();
		assertThat( result ).isNull();
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}


	@Test
	void registerInMock_primitiveArguments() {

		mockitoHandler.registerInMock( listeners, createDelegate( "primitiveArguments" ) );

		mock.primitiveArguments( 1, 2L, (char) 3, (short) 5, (byte) 6, 7f, 8d, true, "string" );
		assertThat( invocationArguments ).containsExactly( 1, 2L, (char) 3, (short) 5, (byte) 6, 7f, 8d, true, "string" );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}


	@Test
	void registerInMock_varargsObject() {

		mockitoHandler.registerInMock( listeners, createDelegate( "varargObject" ) );

		mock.varargObject( "v1", 3L, 3, 'c' );
		assertThat( invocationArguments ).containsExactly( "v1", 3L, 3, 'c' );
		assertThat( usedListenerContainers ).containsExactly( listeners );
	}


	@Test
	void registerInMock_varargsInt() {

		mockitoHandler.registerInMock( listeners, createDelegate( "varargInt" ) );

		mock.varargInt( 3, 5, 8, 13, 21 );
		assertThat( invocationArguments ).containsExactly( 3, 5, 8, 13, 21 );
		assertThat( usedListenerContainers ).containsExactly( listeners );
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
		return null;
	}


	private Method findMethod( String methodName ) {
		return Arrays.stream( mock.getClass().getDeclaredMethods() ).filter( m -> m.getName().equals( methodName ) ).findFirst().get();
	}
}

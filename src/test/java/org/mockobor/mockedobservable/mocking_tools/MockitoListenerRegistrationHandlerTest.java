package org.mockobor.mockedobservable.mocking_tools;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class MockitoListenerRegistrationHandlerTest extends ListenerRegistrationHandler_TestBase {

	@Override
	protected TestMethods createMock() {
		return Mockito.mock( TestMethods.class );
	}

	@Override
	protected MockitoListenerRegistrationHandler createListenerRegistrationHandler() {
		return new MockitoListenerRegistrationHandler();
	}

	@Override
	protected Object createPartialMock( @NonNull Object object ) {
		return Mockito.spy( object );
	}

	@Override
	protected void endOfStubbingMode( Object mock ) {
		// Mockito don't need to change mode
	}


	// =================================================================================
	// ========================= Mockito specific tests ================================
	// =================================================================================

	@Test
	void registerInMock_varargsObject() {

		handler.registerInMock( listeners, createDelegate( "varargObject" ) );
		endOfStubbingMode( mock );

		mock.varargObject( "p1", 2f, null, 3L, 3, 'c' );
		mock.varargObject( null, 22f, "v1", 33L, 44, 'd' );
		mock.varargObject( "p3", (Object) null );
		mock.varargObject( "p4", (Object[]) null ); // mockito's any(Object[].class) does not match "(Object[]) null" now
		mock.varargObject( "p5" );

		assertThat( invocationArguments ).containsExactly(
				Arrays.asList( "p1", 2f, null, 3L, 3, 'c' ),
				Arrays.asList( null, 22f, "v1", 33L, 44, 'd' ),
				Arrays.asList( "p3", null ),
				List.of( "p5" ) );
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}

	@Test
	void registerInMock_varargsInt() {

		handler.registerInMock( listeners, createDelegate( "varargInt" ) );
		endOfStubbingMode( mock );

		mock.varargInt( null, 1 );
		mock.varargInt( "p1", 3, 5, 8, 13, 21 );
		mock.varargInt( null );

		assertThat( invocationArguments ).containsExactly(
				Arrays.asList( null, 1 ),
				Arrays.asList( "p1", 3, 5, 8, 13, 21 ),
				Collections.singletonList( null ) );
		assertThat( usedListenerContainers ).containsOnly( listeners );
	}
}

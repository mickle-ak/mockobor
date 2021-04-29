package org.mockobor.mockedobservable.mocking_tools;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.exceptions.MockoborImplementationError;
import org.mockobor.listener_detectors.ListenerSelector;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler.Invocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


class MockitoListenerRegistrationHandler_PreviousInvocations_Test {

	private final TestMethods                 mock    = Mockito.mock( TestMethods.class );
	private final ListenerRegistrationHandler handler = new MockitoListenerRegistrationHandler();


	@Test
	void getPreviouslyRegistrations() {
		// some calls to find them later
		mock.objectArguments( "oa1", "oa2", "oa3" );
		mock.stringArgument( "sa1" );
		mock.returnType();
		mock.varargObject( "values:", "1", 2d, ListenerSelector.selector( 1, 2, "3" ) );
		mock.varargInt( "values:", 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 );

		Collection<Invocation> invocations = handler.getPreviouslyRegistrations( mock );
		assertThat( invocations )
				.extracting( i -> i.getInvokedMethod().getName(), i -> Arrays.asList( i.getArguments() ) )
				.containsExactly(
						tuple( "objectArguments", Arrays.asList( "oa1", "oa2", "oa3" ) ),
						tuple( "stringArgument", Collections.singletonList( "sa1" ) ),
						tuple( "returnType", Collections.emptyList() ),
						tuple( "varargObject", Arrays.asList( "values:", "1", 2d, ListenerSelector.selector( 1, 2, "3" ) ) ),
						tuple( "varargInt", Arrays.asList( "values:", 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 ) )
				);
	}

	@Test
	void getPreviouslyRegistrations_ignoreStubInvocations() {
		int rc1 = mock.int2int( 100 );
		when( mock.int2int( anyInt() ) ).thenReturn( -1 );
		when( mock.int2int( 200 ) ).thenReturn( -200 );
		int rc2 = mock.int2int( 10 );
		int rc3 = mock.int2int( 200 );
		doReturn( 5 ).when( mock ).int2int( -5 );
		int rc4 = mock.int2int( -5 );

		Collection<Invocation> invocations = handler.getPreviouslyRegistrations( mock );
		assertThat( invocations )
				.extracting( i -> i.getInvokedMethod().getName(), i -> Arrays.asList( i.getArguments() ) )
				.containsExactly(
						tuple( "int2int", Collections.singletonList( 100 ) ),
						tuple( "int2int", Collections.singletonList( 10 ) ),
						tuple( "int2int", Collections.singletonList( 200 ) ),
						tuple( "int2int", Collections.singletonList( -5 ) )
				);
		assertThat( Arrays.asList( rc1, rc2, rc3, rc4 ) ).containsExactly( 0, -1, -200, 5 );
	}


	@SuppressWarnings( "java:S5778" ) // suppress "Refactor the code of the lambda to have only one invocation possibly throwing a runtime exception"
	@Test
	void getPreviouslyRegistrations_notMock() {
		assertThatThrownBy( () -> handler.getPreviouslyRegistrations( new Object() ) )
				.isInstanceOf( MockoborImplementationError.class )
				.hasMessageContainingAll( "mockito", "mock" );
	}
}

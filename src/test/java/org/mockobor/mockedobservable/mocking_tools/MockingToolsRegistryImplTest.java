package org.mockobor.mockedobservable.mocking_tools;

import org.easymock.EasyMock;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.exceptions.MockoborIllegalArgumentException;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MockingToolsRegistryImplTest {

	private final MockingToolsRegistryImpl mockingToolsRegistry = new MockingToolsRegistryImpl();

	@AfterEach
	void tearDown() {
		mockingToolsRegistry.reset();
	}


	@Test
	void findHandlerForMock_Mockito() {
		assertThat( mockingToolsRegistry.findHandlerForMock( Mockito.mock( Object.class ) ) )
				.isInstanceOf( MockitoListenerRegistrationHandler.class );
		assertThat( mockingToolsRegistry.findHandlerForMock( Mockito.spy( Object.class ) ) )
				.isInstanceOf( MockitoListenerRegistrationHandler.class );
	}

	@Test
	void findHandlerForMock_EasyMock() {
		assertThat( mockingToolsRegistry.findHandlerForMock( EasyMock.mock( Object.class ) ) )
				.isInstanceOf( EasymockListenerRegistrationHandler.class );
		assertThat( mockingToolsRegistry.findHandlerForMock( EasyMock.partialMockBuilder( Object.class ).createMock() ) )
				.isInstanceOf( EasymockListenerRegistrationHandler.class );
	}

	@SuppressWarnings( "java:S5778" )
	@Test
	void findHandlerForMock_mock_not_found() {
		assertThatThrownBy( () -> mockingToolsRegistry.findHandlerForMock( new Object() ) )
				.isInstanceOf( MockingToolNotDetectedException.class );
	}


	// ==================================================================================
	// ================================== add custom ====================================
	// ==================================================================================

	public static class AnotherListenerRegistrationHandler implements ListenerRegistrationHandler {

		@Override
		public boolean canHandle( Object mockedObservable ) {
			return mockedObservable != null && mockedObservable.getClass() == Object.class;
		}

		@Override
		public void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registration ) {}
	}


	@Test
	void registerAnotherMockingTool() {
		// Object as mocking tool :-), but always in classpath
		boolean rc = mockingToolsRegistry.registerListenerRegistrationHandler( new AnotherListenerRegistrationHandler() );
		assertThat( rc ).isTrue();
		assertThat( mockingToolsRegistry.findHandlerForMock( new Object() ) )
				.isInstanceOf( AnotherListenerRegistrationHandler.class );
	}

	// suppress "Refactor the code of the lambda to have only one invocation, possibly throwing a runtime exception"
	@SuppressWarnings( "java:S5778" )
	@Test
	void registerUnknownMockingTool() {
		// mocking tool not in classpath
		boolean rc = mockingToolsRegistry.addDefaultMockingTool( "unknown.class.name", AnotherListenerRegistrationHandler.class.getName() );
		assertThat( rc ).isFalse();
		assertThatThrownBy( () -> mockingToolsRegistry.findHandlerForMock( new Object() ) )
				.isInstanceOf( MockingToolNotDetectedException.class );
	}

	// suppress "Refactor the code of the lambda to have only one invocation, possibly throwing a runtime exception"
	@SuppressWarnings( "java:S5778" )
	@Test
	void register_invalid_handle_not_implemented_ListenerRegistrationHandler() {
		// Object as mocking tool :-), but always in classpath
		assertThatThrownBy( () -> mockingToolsRegistry.addDefaultMockingTool( Object.class.getName(), Object.class.getName() ) )
				.isInstanceOf( MockoborIllegalArgumentException.class );
	}

	// suppress "Refactor the code of the lambda to have only one invocation, possibly throwing a runtime exception"
	@SuppressWarnings( "java:S5778" )
	@Test
	void register_invalid_handle_unknownHandlerClass() {
		// Object as mocking tool :-), but always in classpath
		assertThatThrownBy( () -> mockingToolsRegistry.addDefaultMockingTool( Object.class.getName(), "org.mockobor.unknown.HandlerClass" ) )
				.isInstanceOf( MockoborIllegalArgumentException.class );
	}
}

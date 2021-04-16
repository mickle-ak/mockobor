package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.exceptions.MockoborIllegalArgumentException;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


class MockingToolsRegistryImplTest {

	private final MockingToolsRegistry mockingToolsRegistry = new MockingToolsRegistryImpl();

	@AfterEach
	void tearDown() {
		mockingToolsRegistry.reset();
	}


	@Test
	void findHandlerForMock() {
		assertThat( mockingToolsRegistry.findHandlerForMock( mock( Object.class ) ) )
				.isInstanceOf( MockitoListenerRegistrationHandler.class );
		assertThat( mockingToolsRegistry.findHandlerForMock( spy( Object.class ) ) )
				.isInstanceOf( MockitoListenerRegistrationHandler.class );
	}

	@SuppressWarnings( "java:S5778" )
	@Test
	void findHandlerForMock_mock_not_found() {
		assertThatThrownBy( () -> mockingToolsRegistry.findHandlerForMock( new Object() ) )
				.isInstanceOf( MockingToolNotDetectedException.class );
	}


	public static class AnotherListenerRegistrationHandler implements ListenerRegistrationHandler {

		@Override
		public boolean canHandle( Object mockedObservable ) {
			return mockedObservable.getClass() == Object.class;
		}

		@Override
		public void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registration ) {}
	}


	@Test
	void registerAnotherMockingTool() {
		// Object as mocking tool :-), but always in classpath
		boolean rc = mockingToolsRegistry.registerMockingTool( Object.class.getName(), AnotherListenerRegistrationHandler.class.getName() );
		assertThat( rc ).isTrue();
		assertThat( mockingToolsRegistry.findHandlerForMock( new Object() ) )
				.isInstanceOf( AnotherListenerRegistrationHandler.class );
	}

	@SuppressWarnings( "java:S5778" )
	@Test
	void registerUnknownMockingTool() {
		// mocking tool not in classpath
		boolean rc = mockingToolsRegistry.registerMockingTool( "unknown.class.name", AnotherListenerRegistrationHandler.class.getName() );
		assertThat( rc ).isFalse();
		assertThatThrownBy( () -> mockingToolsRegistry.findHandlerForMock( new Object() ) )
				.isInstanceOf( MockingToolNotDetectedException.class );
	}

	@SuppressWarnings( "java:S5778" )
	@Test
	void register_invalid_handle_not_implemented_ListenerRegistrationHandler() {
		// Object as mocking tool :-), but always in classpath
		assertThatThrownBy( () -> mockingToolsRegistry.registerMockingTool( Object.class.getName(), Object.class.getName() ) )
				.isInstanceOf( MockoborIllegalArgumentException.class );
	}
}

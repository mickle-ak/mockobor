package org.mockobor.mockedobservable.mocking_tools;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class EasyMock_ToolsRegistry_Test {

	private final MockingToolsRegistryImpl mockingToolsRegistry = new MockingToolsRegistryImpl();

	@Test
	void only_easymock_is_registered() {
		assertThat( mockingToolsRegistry.getAvailableHandlers() ).hasSize( 1 );
		assertThat( mockingToolsRegistry.getAvailableHandlers().get( 0 ) )
				.isInstanceOf( EasymockListenerRegistrationHandler.class );
	}

	@Test
	void easymock_mock_can_be_handled() {
		assertThat( mockingToolsRegistry.findHandlerForMock( EasyMock.mock( Object.class ) ) )
				.isInstanceOf( EasymockListenerRegistrationHandler.class );
	}
}

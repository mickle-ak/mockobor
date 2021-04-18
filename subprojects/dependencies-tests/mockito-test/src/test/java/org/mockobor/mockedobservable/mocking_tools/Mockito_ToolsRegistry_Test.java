package org.mockobor.mockedobservable.mocking_tools;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;


class Mockito_ToolsRegistry_Test {

    private final MockingToolsRegistryImpl mockingToolsRegistry = new MockingToolsRegistryImpl();

    @Test
    void only_mockito_is_registered() {
        assertThat(mockingToolsRegistry.getAvailableHandlers()).hasSize(1);
        assertThat(mockingToolsRegistry.getAvailableHandlers().get(0))
                .isInstanceOf(MockitoListenerRegistrationHandler.class);
    }

    @Test
    void mockito_mock_can_be_handled() {
        assertThat( mockingToolsRegistry.findHandlerForMock( Mockito.mock( Object.class ) ) )
                .isInstanceOf( MockitoListenerRegistrationHandler.class );
    }
}

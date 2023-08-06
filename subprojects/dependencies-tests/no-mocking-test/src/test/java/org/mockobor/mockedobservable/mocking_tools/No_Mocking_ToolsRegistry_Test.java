package org.mockobor.mockedobservable.mocking_tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class No_Mocking_ToolsRegistry_Test {

	@Test
	void no_mocking_tools_registered_because_no_mocking_tools_in_classpath() {
		assertThat( new MockingToolsRegistryImpl().getAvailableHandlers() ).isEmpty();
	}

	@Test
	void canNotCreate_Mockito() {
		assertThatThrownBy( MockitoListenerRegistrationHandler::new )
				.hasCauseInstanceOf( NoClassDefFoundError.class )
				.hasMessageFindingMatch( "org.mockito" );
	}

	@Test
	void canNotCreate_EasyMock() {
		assertThatThrownBy( EasymockListenerRegistrationHandler::new )
				.hasCauseInstanceOf( NoClassDefFoundError.class )
				.hasMessageFindingMatch( "org.easymock" );
	}
}

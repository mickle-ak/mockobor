package org.mockobor.listener_detectors;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class ListenerSelectorTest {

	@Test
	void createSelector() {
		assertThat( selector( "a", 2, 3d ).toString() ).contains( "a", "2", "3" );
	}

	@Test
	void createSelector_nullValue() {
		assertThat( selector( (Object) null ) ).isNotEqualTo( selector() ).isEqualTo( selector( (Object[]) null ) );

		assertThat( selector( (Object) null ).toString() ).contains( "null" );
		assertThat( selector( (Object[]) null ).toString() ).contains( "null" );

		assertThat( selector( null, null ) ).isNotEqualTo( selector( (Object) null ) ).isNotEqualTo( selector() );
		assertThat( selector( null, null ).toString() ).contains( "null, null" );
	}
}

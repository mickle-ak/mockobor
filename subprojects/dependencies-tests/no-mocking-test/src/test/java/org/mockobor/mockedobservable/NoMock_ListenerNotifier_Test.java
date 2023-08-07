package org.mockobor.mockedobservable;

import org.junit.jupiter.api.Test;
import org.mockobor.Mockobor;
import org.mockobor.exceptions.MockingToolNotDetectedException;

import java.beans.PropertyChangeSupport;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class NoMock_ListenerNotifier_Test {

	@Test
	void createNotifierFor_failed() {
		PropertyChangeSupport observable = new PropertyChangeSupport( new Object() );
		assertThatThrownBy( () -> Mockobor.createNotifierFor( observable ) )
				.isInstanceOf( MockingToolNotDetectedException.class );
	}
}

package org.mockobor.mockedobservable;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.mockobor.Mockobor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


class EasyMock_ListenerNotifier_Test {

	private static class ClassUnderTest {

		private final List<PropertyChangeEvent> invocations = new ArrayList<>();

		public ClassUnderTest( PropertyChangeSupport observable ) {
			observable.addPropertyChangeListener( invocations::add );
		}

		public List<PropertyChangeEvent> getInvocations() {
			return invocations;
		}
	}


	@Test
	void createNotifierFor() {
		PropertyChangeSupport mockedObservable = EasyMock.mock( PropertyChangeSupport.class );
		PropertyChangeNotifier notifierFor = (PropertyChangeNotifier) Mockobor.createNotifierFor( mockedObservable );
		EasyMock.replay( mockedObservable );

		ClassUnderTest classUnderTest = new ClassUnderTest( mockedObservable );

		notifierFor.firePropertyChange( "prop", "o1", "n1" );

		assertThat( classUnderTest.getInvocations() )
				.hasSize( 1 )
				.extracting( PropertyChangeEvent::getPropertyName, PropertyChangeEvent::getOldValue, PropertyChangeEvent::getNewValue )
				.containsExactly( tuple( "prop", "o1", "n1" ) );
	}
}

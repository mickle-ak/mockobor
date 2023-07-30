package org.mockobor.mockedobservable;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockobor.Mockobor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockobor.mockedobservable.UsageExample_BeanPropertyChange_Test.ObservableBean.ANOTHER_PROPERTY_NAME;
import static org.mockobor.mockedobservable.UsageExample_BeanPropertyChange_Test.ObservableBean.PROPERTY_NAME;


@SuppressWarnings( "unused" )
class UsageExample_BeanPropertyChange_Test {

	/** The beans (that support bound properties) to mock. */
	public interface ObservableBean {

		String PROPERTY_NAME         = "propertyName";
		String ANOTHER_PROPERTY_NAME = "anotherPropertyName";

		void addPropertyChangeListener( PropertyChangeListener listener );

		void addPropertyChangeListener( String propertyName, PropertyChangeListener listener );

		void removePropertyChangeListener( PropertyChangeListener listener );

		void removePropertyChangeListener( String propertyName, PropertyChangeListener listener );
	}


	/** The class under test, which observes a {@link ObservableBean}. */
	public static class BeanObserverUnderTest implements PropertyChangeListener, AutoCloseable {

		private final ObservableBean observable;

		private final List<PropertyChangeEvent> propertyChangeEvents = new ArrayList<>();


		public BeanObserverUnderTest( ObservableBean observable ) {
			this.observable = observable;
			this.observable.addPropertyChangeListener( this );
			this.observable.addPropertyChangeListener( PROPERTY_NAME, this );
		}

		@Override
		public void close() {
			this.observable.removePropertyChangeListener( this );
			this.observable.removePropertyChangeListener( PROPERTY_NAME, this );
		}


		@Override
		public void propertyChange( PropertyChangeEvent evt ) {
			assertThat( evt.getSource() ).as( "correct source" ).isSameAs( observable );
			propertyChangeEvents.add( evt );
		}

		public List<PropertyChangeEvent> getPropertyChangeEvents() { return propertyChangeEvents; }
	}


	//-----------------------------------------------------------------------------------------
	//---------------------------------------  tests  -----------------------------------------
	//-----------------------------------------------------------------------------------------

	private PropertyChangeNotifier notifier;

	private BeanObserverUnderTest observer;


	@BeforeEach
	void setUp() {
		// Create mock for ObservableBean.
		ObservableBean mockedObservable = mock( ObservableBean.class );

		// Create notifier for mocked observable bean. For bean with PropertyChangeSupport as mock,
		// it returns the special interface "PropertyChangeNotifier".
		notifier = (PropertyChangeNotifier) Mockobor.createNotifierFor( mockedObservable );

		// Create SUT-object, which observes the mocked ObservableBean
		observer = new BeanObserverUnderTest( mockedObservable );
	}


	@Test
	void receiveUpdatesFromMockedObservable() {

		// Simulate calling of change notifications from mocked observable
		notifier.firePropertyChange( PROPERTY_NAME, "oldValue1", "newValue1" );
		notifier.firePropertyChange( ANOTHER_PROPERTY_NAME, "oldValue2", "newValue2" );
		notifier.firePropertyChange( PROPERTY_NAME, 1, 2 );


		// Check that observer has received the notifications from mocked observable
		assertThat( observer.getPropertyChangeEvents() )
			.extracting( PropertyChangeEvent::getPropertyName, PropertyChangeEvent::getOldValue, PropertyChangeEvent::getNewValue )
			.containsExactly(
				tuple( PROPERTY_NAME, "oldValue1", "newValue1" ), // common listener
				tuple( PROPERTY_NAME, "oldValue1", "newValue1" ), // named listener
				tuple( ANOTHER_PROPERTY_NAME, "oldValue2", "newValue2" ), // only common listener
				tuple( PROPERTY_NAME, 1, 2 ), // common listener
				tuple( PROPERTY_NAME, 1, 2 ) // named listener
			);
	}


	@Test
	void checkDeregistrationOfAllListeners() {
		// close should deregister all listeners
		observer.close();

		// check that all listeners are unregistered
		Mockobor.assertThatAllListenersAreUnregistered( notifier );
	}


	@Test
	void numberOfRegisteredListeners() {
		assertThat( notifier.numberOfRegisteredListeners() ).as( "number of registered listeners" ).isEqualTo( 2 );
		assertThat( notifier.numberOfListenerRegistrations() ).as( "number of registration" ).isEqualTo( 2 );
		assertThat( notifier.numberOfListenerDeregistrations() ).as( "number of deregistrations" ).isZero();
		assertThat( notifier.getAllListeners() ).hasSize( 2 ).allMatch( l -> l == observer );

		// close should deregister all listeners
		observer.close();

		assertThat( notifier.numberOfRegisteredListeners() ).as( "number of registered listeners after close" ).isZero();
		assertThat( notifier.numberOfListenerRegistrations() ).as( "number of registration" ).isEqualTo( 2 );
		assertThat( notifier.numberOfListenerDeregistrations() ).as( "number of deregistrations" ).isEqualTo( 2 );
		assertThat( notifier.getAllListeners() ).isEmpty();
	}
}

package org.mockobor.mockedobservable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockobor.Mockobor;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class UsageExample_Observable_Test {

	/** The class under test, which should observes a mocked {@link Observable}. */
	public static class ObserverUnderTest implements Observer, AutoCloseable {

		private final Observable observable;

		private final List<Object> updateArguments = new ArrayList<>();

		public ObserverUnderTest( Observable observable ) {
			this.observable = observable;
			this.observable.addObserver( this );
		}

		@Override
		public void close() { observable.deleteObserver( this ); }


		@Override
		public void update( Observable o, Object arg ) {
			assertThat( o ).as( "correct observable object" ).isSameAs( observable );
			updateArguments.add( arg );
		}


		public List<Object> getUpdateArguments() { return updateArguments; }
	}


	//-----------------------------------------------------------------------------------------
	//---------------------------------------  tests  -----------------------------------------
	//-----------------------------------------------------------------------------------------

	private ObservableNotifier notifier;

	private ObserverUnderTest observer;


	@BeforeEach
	void setUp() {
		// Create mock for Observable.
		Observable mockedObservable = mock( Observable.class );

		// Create notifier for mocked observable. For "Observer" as mock it returns special interface "ObservableNotifier".
		notifier = (ObservableNotifier) Mockobor.createNotifierFor( mockedObservable );

		// Create SUT-object, which registers itself as observer for the mocked Observable and close to deregister
		observer = new ObserverUnderTest( mockedObservable );
	}


	@Test
	void receiveUpdatesFromMockedObservable() {

		// Simulate calling of change notifications from mocked observable
		notifier.notifyObservers();
		notifier.notifyObservers( "update parameter" );

		// Check that observer has receive the notifications from mocked observable
		assertThat( observer.getUpdateArguments() ).containsExactly( null, "update parameter" );
	}


	@Test
	void checkDeregistrationOfAllListeners() {
		// close should deregister all listeners
		observer.close();

		// check that all listeners are neatly unregistered
		Mockobor.assertThatAllListenersAreUnregistered( notifier );
	}


	@Test
	void numberOfRegisteredListeners() {
		assertThat( notifier.numberOfRegisteredListeners() ).as( "number of registered listeners" ).isEqualTo( 1 );
		assertThat( notifier.numberOfListenerRegistrations() ).as( "number of registration" ).isEqualTo( 1 );
		assertThat( notifier.numberOfListenerDeregistrations() ).as( "number of deregistrations" ).isZero();
		assertThat( notifier.getAllListeners() ).hasSize( 1 ).containsExactly( observer );

		// close should deregister all listeners
		observer.close();

		assertThat( notifier.numberOfRegisteredListeners() ).as( "number of registered listeners after close" ).isZero();
		assertThat( notifier.numberOfListenerRegistrations() ).as( "number of registration" ).isEqualTo( 1 );
		assertThat( notifier.numberOfListenerDeregistrations() ).as( "number of deregistrations" ).isEqualTo( 1 );
		assertThat( notifier.getAllListeners() ).isEmpty();
	}
}

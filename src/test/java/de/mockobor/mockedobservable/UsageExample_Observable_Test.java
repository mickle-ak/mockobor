package de.mockobor.mockedobservable;

import de.mockobor.mockedobservable.api.Mockitobor;
import de.mockobor.mockedobservable.api.ObservableNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	private Observable mockedObservable;

	private ObservableNotifier notifier;


	@BeforeEach
	void setUp() {
		// Create mock for Observable.
		mockedObservable = mock( Observable.class );

		// Prepare mocked observable to observation. For "Observer" as mock it returns special interface "ObservableNotifier".
		notifier = Mockitobor.startObservation( mockedObservable );
	}


	@Test
	void receiveUpdatesFromMockedObservable() {
		// Create SUT-object, which observes the mocked Observable
		ObserverUnderTest observer = new ObserverUnderTest( mockedObservable );


		// Simulate calling of change notifications from mocked observable
		notifier.notifyObservers();
		notifier.notifyObservers( "update parameter" );


		// Check that observer has receive the notifications from mocked observable
		assertThat( observer.getUpdateArguments() ).containsExactly( null, "update parameter" );
	}


	@Test
	void checkDeregistrationOfAllListeners() {
		// Create SUT-object, which registers itself as observer for the mocked Observable and close to deregister
		ObserverUnderTest observer = new ObserverUnderTest( mockedObservable );
		observer.close();

		// check that all listeners are neatly unregistered
		assertThat( notifier.allListenersAreDeregistered() ).as( "all listeners are deregistered" ).isTrue();
	}
}

package org.mockobor.mockedobservable;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockobor.Mockobor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


@SuppressWarnings( "unused" )
class UsageExample_TypicalJavaListener_Test {

	/** The listener to listen to changes on something. */
	public interface MyListener {
		void somethingChanged1( Object somethingNewValue );
		void somethingChanged2( Object somethingNewValue );
	}

	/** The listener to listen to changes on something else. */
	public interface MyAnotherListener {
		void somethingOtherChanged( Object somethingOtherValue );
	}


	/** The class (that support notification on change something and something else) to mock. */
	public interface ObservableObject {

		void addMyListener( MyListener listener );

		void addMyListener( String selector, MyListener listener );

		void addMyAnotherListener( MyAnotherListener listener );

		void removeMyListener( MyListener listener );

		void removeMyListener( String selector, MyListener listener );

		void removeMyAnotherListener( MyAnotherListener listener );
	}


	/** The class under test, which observes a {@link ObservableObject}. */
	public static class BeanObserverUnderTest implements MyListener, MyAnotherListener, AutoCloseable {

		private final ObservableObject observable;

		private final List<Object> allChangesTogether = new ArrayList<>();


		public BeanObserverUnderTest( ObservableObject observable ) {
			this.observable = observable;
			this.observable.addMyListener( this );
			this.observable.addMyListener( "selValue", this );
			this.observable.addMyAnotherListener( this );
		}

		@Override
		public void close() {
			this.observable.removeMyListener( this );
			this.observable.removeMyListener( "selValue", this );
			this.observable.removeMyAnotherListener( this );
		}

		@Override
		public void somethingChanged1( Object somethingNewValue ) {
			allChangesTogether.add( somethingNewValue );
		}

		@Override
		public void somethingChanged2( Object somethingNewValue ) {
			allChangesTogether.add( somethingNewValue );
		}

		@Override
		public void somethingOtherChanged( Object somethingOtherValue ) {
			allChangesTogether.add( somethingOtherValue );
		}

		public List<Object> getAllChangesTogether() { return allChangesTogether; }
	}


	//-----------------------------------------------------------------------------------------
	//---------------------------------------  tests  -----------------------------------------
	//-----------------------------------------------------------------------------------------

	private ListenersNotifier notifier;

	private BeanObserverUnderTest observer;


	@BeforeEach
	void setUp() {
		// Create mock for ObservableObject.
		ObservableObject mockedObservable = mock( ObservableObject.class );

		// Create notifier for the mocked observable object.
		// For classes with typical Java listener, it returns listener interfaces itself.
		notifier = Mockobor.createNotifierFor( mockedObservable );

		// Create SUT-object, which observes the mocked ObservableObject
		observer = new BeanObserverUnderTest( mockedObservable );
	}


	@Test
	void receiveUpdatesFromMockedObservable() {

		// Simulate calling of change notifications from mocked observable using listener interfaces
		( (MyListener) notifier ).somethingChanged1( "v1" );
		( (MyAnotherListener) notifier ).somethingOtherChanged( "v2" );
		( (MyListener) notifier ).somethingChanged2( "v3" );

		// Simulate calling of change notifications from mocked observable using ListenerNotifier.notifierFor
		notifier.notifierFor( MyListener.class ).somethingChanged2( "v4" );
		notifier.notifierFor( MyListener.class, selector() ).somethingChanged1( "s() v4" ); // same as above

		// Simulate calling of change notifications from mocked observable using ListenerNotifier.notifierFor with selectors
		notifier.notifierFor( "selValue", MyListener.class ).somethingChanged1( "s(selValue) v5" );
		notifier.notifierFor( MyListener.class, selector( "selValue" ) ).somethingChanged1( "v5 s(selValue)" ); // same as above

		// Simulate simultaneous calling of change notifications listeners registered with different selectors
		notifier.notifierFor( MyListener.class, selector(), selector( "selValue" ) ).somethingChanged1( "v5 s(), s(selValue)" );

		// Check that observer has received the notifications from mocked observable
		assertThat( observer.getAllChangesTogether() )
				.containsExactly( "v1", "v2", "v3",
				                  "v4", "s() v4",
				                  "s(selValue) v5", "v5 s(selValue)",
				                  "v5 s(), s(selValue)", "v5 s(), s(selValue)" );
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
		assertThat( notifier.numberOfRegisteredListeners() ).as( "number of registered listeners" ).isEqualTo( 3 );
		assertThat( notifier.numberOfListenerRegistrations() ).as( "number of registration" ).isEqualTo( 3 );
		assertThat( notifier.numberOfListenerDeregistrations() ).as( "number of deregistrations" ).isZero();
		assertThat( notifier.getAllListeners() ).hasSize( 3 ).allMatch( l -> l == observer );

		// close should deregister all listeners
		observer.close();

		assertThat( notifier.numberOfRegisteredListeners() ).as( "number of registered listeners after close" ).isZero();
		assertThat( notifier.numberOfListenerRegistrations() ).as( "number of registration" ).isEqualTo( 3 );
		assertThat( notifier.numberOfListenerDeregistrations() ).as( "number of deregistrations" ).isEqualTo( 3 );
		assertThat( notifier.getAllListeners() ).isEmpty();
	}
}

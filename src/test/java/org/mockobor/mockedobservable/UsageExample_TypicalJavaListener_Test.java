package org.mockobor.mockedobservable;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockobor.Mockobor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@SuppressWarnings( "unused" )
class UsageExample_TypicalJavaListener_Test {

	/** The listener to listen changes on something. */
	public interface MyListener {

		void somethingChanged1( Object somethingNewValue );

		void somethingChanged2( Object somethingNewValue );
	}

	/** The listener to listen changes on something other. */
	public interface MyAnotherListener {

		void somethingOtherChanged( Object somthingOtherValue );
	}


	/** The class (that support notification on change something and something other) to mock. */
	public interface ObservableObject {

		void addMyListener( MyListener listener );

		void addMyAnotherListener( MyAnotherListener listener );

		void removeMyListener( MyListener listener );

		void removeMyAnotherListener( MyAnotherListener listener );
	}


	/** The class under test, which observes a {@link ObservableObject}. */
	public static class BeanObserverUnderTest implements MyListener, MyAnotherListener, AutoCloseable {

		private final ObservableObject observable;

		private final List<Object> allChangesTogether = new ArrayList<>();


		public BeanObserverUnderTest( ObservableObject observable ) {
			this.observable = observable;
			this.observable.addMyListener( this );
			this.observable.addMyAnotherListener( this );
		}

		@Override
		public void close() {
			this.observable.removeMyListener( this );
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

		// Create notifier for mocked observable object. For classes with typical Java listener it returns listener interfaces itself.
		notifier = Mockobor.createNotifierFor( mockedObservable );

		// Create SUT-object, which observes the mocked ObservableObject
		observer = new BeanObserverUnderTest( mockedObservable );
	}


	@Test
	void receiveUpdatesFromMockedObservable() {

		// Simulate calling of change notifications from mocked observable
		( (MyListener) notifier ).somethingChanged1( "value1" );
		( (MyAnotherListener) notifier ).somethingOtherChanged( "value2" );
		( (MyListener) notifier ).somethingChanged2( "value3" );


		// Check that observer has receive the notifications from mocked observable
		assertThat( observer.getAllChangesTogether() ).containsExactly( "value1", "value2", "value3" );
	}


	@Test
	void checkDeregistrationOfAllListeners() {
		// close should deregister all listeners
		observer.close();

		// check that all listeners are unregistered
		assertThat( notifier.allListenersAreUnregistered() ).as( "all listeners are deregistered" ).isTrue();
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

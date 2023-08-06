package org.mockobor.mockedobservable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockobor.exceptions.ListenersNotFoundException;
import org.mockobor.listener_detectors.ListenerSelector;
import org.mockobor.mockedobservable.ListenersNotifier.ListenerKey;
import org.mockobor.mockedobservable.MockedObservable.MyAnotherListener;
import org.mockobor.mockedobservable.MockedObservable.MyListener;
import org.mockobor.mockedobservable.TestedObserver.InvocationDef;
import org.mockobor.mockedobservable.TestedObserver.MyAnotherListenerImpl;
import org.mockobor.mockedobservable.TestedObserver.MyListenerImpl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class ListenerManagerTest {

	private final Object testObservable = mock( Object.class );

	private final ListenersManager listenerManager = new ListenersManager( testObservable );

	private final MyListenerImpl        listener_no_selector       = new MyListenerImpl();
	private final MyListenerImpl        listener_v_selector        = new MyListenerImpl();
	private final MyListenerImpl        listener_v1_v2_selector    = new MyListenerImpl();
	private final MyAnotherListenerImpl anotherListener_v_selector = new MyAnotherListenerImpl();

	@BeforeEach
	void setUp() {
		listenerManager.addListener( selector(), MyListener.class, listener_no_selector );
		listenerManager.addListener( selector( "v" ), MyListener.class, listener_v_selector );
		listenerManager.addListener( selector( "v1", "v2" ), MyListener.class, listener_v1_v2_selector );
		listenerManager.addListener( selector( "v" ), MyAnotherListener.class, anotherListener_v_selector );
	}

	// ==================================================================================
	// ================================= add / remove ===================================
	// ==================================================================================

	@Nested
	class AddRemoveListeners {

		@Test
		void addListeners() {
			assertThat( listenerManager.numberOfRegisteredListeners() ).isEqualTo( 4 );
			assertThat( listenerManager.numberOfListenerRegistrations() ).isEqualTo( 4 );
			assertThat( listenerManager.allListenersAreUnregistered() ).isFalse();

			assertThat( listenerManager.getAllListeners() )
					.containsExactlyInAnyOrder( listener_no_selector, listener_v_selector, listener_v1_v2_selector, anotherListener_v_selector );

			assertThat( listenerManager.getListeners( MyListener.class ) )
					.containsExactlyInAnyOrder( listener_no_selector, listener_v_selector, listener_v1_v2_selector );

			assertThat( listenerManager.getListeners( MyListener.class, selector() ) ).containsExactly( listener_no_selector );
			assertThat( listenerManager.getListeners( MyListener.class, selector( "v" ) ) ).containsExactly( listener_v_selector );
			assertThat( listenerManager.getListeners( MyListener.class, selector( "v1", "v2" ) ) ).containsExactly( listener_v1_v2_selector );
			assertThat( listenerManager.getListeners( MyListener.class, selector(), selector( "v1", "v2" ) ) )
					.containsExactlyInAnyOrder( listener_no_selector, listener_v1_v2_selector );

			assertThat( listenerManager.getListeners( MyAnotherListener.class, selector( "v" ) ) ).containsExactly( anotherListener_v_selector );

			assertThat( listenerManager.getListenersWithSelector() )
					.extracting( ListenerKey::getListenerClass, ListenerKey::getSelector )
					.containsExactlyInAnyOrder(
							tuple( MyListener.class, selector() ),
							tuple( MyListener.class, selector( "v" ) ),
							tuple( MyListener.class, selector( "v1", "v2" ) ),
							tuple( MyAnotherListener.class, selector( "v" ) ) );
		}

		@Test
		void addAlreadyAdded_with_same_selector() {
			listenerManager.addListener( selector( "v" ), MyListener.class, listener_v_selector );

			assertThat( listenerManager.numberOfRegisteredListeners() ).isEqualTo( 5 );
			assertThat( listenerManager.getListeners( MyListener.class, selector( "v" ) ) )
					.as( "the listener is in the list 2 times" )
					.containsExactly( listener_v_selector, listener_v_selector );
		}

		@Test
		void addAlreadyAdded_with_another_selector() {
			listenerManager.addListener( selector( "x" ), MyListener.class, listener_v_selector );

			assertThat( listenerManager.numberOfRegisteredListeners() ).isEqualTo( 5 );
			assertThat( listenerManager.getListeners( MyListener.class, selector( "v" ) ) ).containsExactly( listener_v_selector );
			assertThat( listenerManager.getListeners( MyListener.class, selector( "x" ) ) ).containsExactly( listener_v_selector );
		}

		@Test
		void removeListener() {
			listenerManager.removeListener( selector(), MyListener.class, listener_no_selector );
			listenerManager.removeListener( selector( "v" ), MyListener.class, listener_v_selector );
			listenerManager.removeListener( selector( "v1", "v2" ), MyListener.class, listener_v1_v2_selector );
			listenerManager.removeListener( selector( "v" ), MyAnotherListener.class, anotherListener_v_selector );

			assertThat( listenerManager.getAllListeners() ).isEmpty();
			assertThat( listenerManager.numberOfRegisteredListeners() ).isZero();
			assertThat( listenerManager.numberOfListenerDeregistrations() ).isEqualTo( 4 );
			assertThat( listenerManager.allListenersAreUnregistered() ).isTrue();
			assertThat( listenerManager.getListenersWithSelector() ).isEmpty();
		}

		@Test
		void removeAlreadyRemoved_nothing_happened() {
			listenerManager.removeListener( selector( "v1", "v2" ), MyListener.class, listener_v1_v2_selector );
			assertThat( listenerManager.numberOfRegisteredListeners() ).isEqualTo( 3 );
			assertThat( listenerManager.numberOfListenerDeregistrations() ).isEqualTo( 1 );
			assertThat( listenerManager.getListeners( MyListener.class, selector( "v1", "v2" ) ) ).isEmpty(); // no listeners for selector

			// remove already removed listener
			listenerManager.removeListener( selector( "v1", "v2" ), MyListener.class, listener_v1_v2_selector );

			assertThat( listenerManager.numberOfRegisteredListeners() ).isEqualTo( 3 ); // not changed
			assertThat( listenerManager.numberOfListenerDeregistrations() ).isEqualTo( 1 ); // not changed
			assertThat( listenerManager.getListeners( MyListener.class, selector( "v1", "v2" ) ) ).isEmpty(); // no listeners for selector
		}
	}


	// ==================================================================================
	// =================================== notify =======================================
	// ==================================================================================

	@Nested
	class Notify {

		@Test
		void notifyListeners_no_selector() {
			listenerManager.notifierFor( MyListener.class ).somethingChanged1( "MyListener.somethingChanged1 no selector" );
			listenerManager.notifierFor( MyListener.class, selector() ).somethingChanged1( "MyListener.somethingChanged1 empty selector" );
			listenerManager.notifierFor( MyListener.class, selector() ).somethingChanged2( "MyListener.somethingChanged2 empty selector" );

			assertThat( listener_no_selector.getInvocations() )
					.extracting( InvocationDef::getClazz, InvocationDef::getMethod, InvocationDef::getParam )
					.containsExactly(
							tuple( MyListener.class, "somethingChanged1", "MyListener.somethingChanged1 no selector" ),
							tuple( MyListener.class, "somethingChanged1", "MyListener.somethingChanged1 empty selector" ),
							tuple( MyListener.class, "somethingChanged2", "MyListener.somethingChanged2 empty selector" ) );
		}

		@Test
		void notifyListeners_one_selector() {
			listenerManager.addListener( selector( "x" ), MyListener.class, listener_v_selector );

			listenerManager.notifierFor( "v", MyListener.class ).somethingChanged2( "MyListener.somethingChanged2 v selector" );
			listenerManager.notifierFor( "x", MyListener.class ).somethingChanged2( "MyListener.somethingChanged2 x selector" );
			listenerManager.notifierFor( MyListener.class, selector( "x" ) ).somethingChanged1( "MyListener.somethingChanged2 x selector" );

			assertThat( listener_v_selector.getInvocations() )
					.extracting( InvocationDef::getClazz, InvocationDef::getMethod, InvocationDef::getParam )
					.containsExactly(
							tuple( MyListener.class, "somethingChanged2", "MyListener.somethingChanged2 v selector" ),
							tuple( MyListener.class, "somethingChanged2", "MyListener.somethingChanged2 x selector" ),
							tuple( MyListener.class, "somethingChanged1", "MyListener.somethingChanged2 x selector" ) );
		}

		@Test
		void notifyListeners_multi_selectors() {
			listenerManager.notifierFor( MyListener.class, selector(), selector( "v" ), selector( "v1", "v2" ) ).somethingChanged1( "multi" );
			listenerManager.notifierFor( MyAnotherListener.class, selector(), selector( "v" ), selector( "v1", "v2" ) ).somethingOtherChanged( "multi2" );

			assertThat( listener_no_selector.getInvocations() ).extracting( InvocationDef::getParam ).containsExactly( "multi" );
			assertThat( listener_v_selector.getInvocations() ).extracting( InvocationDef::getParam ).containsExactly( "multi" );
			assertThat( listener_v1_v2_selector.getInvocations() ).extracting( InvocationDef::getParam ).containsExactly( "multi" );
			assertThat( anotherListener_v_selector.getInvocations() ).extracting( InvocationDef::getParam ).containsExactly( "multi2" );
		}

		@Test
		void notifyListeners_multi_selectors_inclusive_unknown_selectors() {
			listenerManager.notifierFor( MyAnotherListener.class, selector(), selector( "v" ), selector( "v1", "v2" ) )
			               .somethingOtherChanged( "multi" );

			// MyAnotherListener registered only on time => only one invocation
			assertThat( anotherListener_v_selector.getInvocations() ).extracting( InvocationDef::getParam ).containsExactly( "multi" );

			assertThat( listener_no_selector.getInvocations() ).isEmpty();
			assertThat( listener_v_selector.getInvocations() ).isEmpty();
			assertThat( listener_v1_v2_selector.getInvocations() ).isEmpty();
		}

		@Test
		void notifyListeners_distinct_selectors() {
			listenerManager.notifierFor( MyListener.class, selector(), selector( "v" ), selector(), selector( "v" ) ).somethingChanged1( "something" );
			assertThat( listener_no_selector.getInvocations() ).hasSize( 1 );
			assertThat( listener_v_selector.getInvocations() ).hasSize( 1 );
		}

		@Test
		void notifyListeners_return_value_if_notify_multi_selectors() {
			listenerManager.notifierFor( MyListener.class, selector(), selector( "v" ), selector() ).somethingChanged2( "something" );
			int rc = listenerManager.notifierFor( MyListener.class, selector(), selector( "v" ), selector() ).somethingChanged2( "something else" );
			assertThat( rc ).as( "2 calls => 2 notifications" ).isEqualTo( 2 );
		}

		@Test
		void notifyListeners_return_value_if_no_listener_found() {
			listenerManager.setStrictCheckListenerList( false );
			int rc = listenerManager.notifierFor( MyListener.class, selector( "unknown" ) ).somethingChanged2( "something" );
			assertThat( rc ).as( "default primitive value" ).isZero();
		}

		@Test
		void notifyListeners_exception_if_no_listener_found() {
			ListenerSelector unknownSelector = selector( "unknown" );
			assertThatThrownBy( () -> listenerManager.notifierFor( MyListener.class, unknownSelector ) )
					.isInstanceOf( ListenersNotFoundException.class );
		}
	}
}

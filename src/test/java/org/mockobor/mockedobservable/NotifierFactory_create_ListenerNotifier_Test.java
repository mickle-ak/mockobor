package org.mockobor.mockedobservable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockobor.exceptions.MockoborException;
import org.mockobor.listener_detectors.ListenerDetectorsRegistryImpl;
import org.mockobor.listener_detectors.ListenerSelector;
import org.mockobor.mockedobservable.MockedObservable.MyAnotherListener;
import org.mockobor.mockedobservable.MockedObservable.MyListener;
import org.mockobor.mockedobservable.TestedObserver.InvocationDef;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistryImpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class NotifierFactory_create_ListenerNotifier_Test {

	private final MockedObservable mockedObservable = mock( MockedObservable.class );

	private ListenersNotifier notifier;
	private TestedObserver    testedObserver;

	@BeforeEach
	void setUp() {
		notifier = new NotifierFactory( new ListenerDetectorsRegistryImpl(),
		                                new MockingToolsRegistryImpl() )
				.create( mockedObservable );
		testedObserver = new TestedObserver( mockedObservable );
	}


	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	@Test
	void ListenersNotifier_all_methods_throw_no_exception() {

		assertThat( notifier.getObservableMock() ).isSameAs( mockedObservable );
		assertThat( notifier.allListenersAreUnregistered() ).isFalse();
		assertThat( notifier.numberOfRegisteredListeners() ).isEqualTo( 6 );
		assertThat( notifier.numberOfListenerRegistrations() ).isEqualTo( 6 );
		assertThat( notifier.numberOfListenerDeregistrations() ).isZero();

		assertThat( notifier.getAllListeners() )
				.hasSize( 6 )
				.containsOnly( testedObserver.getObserver(),
				               testedObserver.getPropertyChangeListener(),
				               testedObserver.getMyListener(),
				               testedObserver.getMyAnotherListener() );

		assertThat( notifier.getListeners( PropertyChangeListener.class ) )
				.hasSize( 2 )
				.containsOnly( testedObserver.getPropertyChangeListener() );

		assertThat( notifier.getListeners( MyListener.class ) )
				.hasSize( 2 )
				.containsOnly( testedObserver.getMyListener() );

		assertThat( notifier.getListeners( MyListener.class, selector( "presel", "postsel" ) ) )
				.containsExactly( testedObserver.getMyListener() );

		assertThat( notifier ).isInstanceOf( ListenersNotifier.class )
		                      .isInstanceOf( ObservableNotifier.class )
		                      .isInstanceOf( PropertyChangeNotifier.class )
		                      .isInstanceOf( Observer.class )
		                      .isInstanceOf( PropertyChangeListener.class )
		                      .isInstanceOf( MyListener.class )
		                      .isInstanceOf( MyAnotherListener.class );

		notifier.setStrictCheckListenerList( false );
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( MyListener.class ) );
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( "sel", MyListener.class ) );
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( MyListener.class, selector( "presel", "postsel" ) ) );
		assertThatNoException().as( "Object's method" ).isThrownBy( () -> notifier.toString() );
	}

	@Test
	void destroy() {
		testedObserver.destroy();

		// follow notifications should be ignored
		notifier.setStrictCheckListenerList( false ); // to avoid exception if no listener found       
		( (ObservableNotifier) notifier ).notifyObservers( "v1" );
		( (PropertyChangeNotifier) notifier ).firePropertyChange( "prop", "o2", "n2" );
		( (MyListener) notifier ).somethingChanged1( "v1" );


		assertThat( notifier.allListenersAreUnregistered() ).isTrue();
		assertThat( notifier.numberOfRegisteredListeners() ).isZero();
		assertThat( notifier.numberOfListenerRegistrations() ).isEqualTo( 6 );
		assertThat( notifier.numberOfListenerDeregistrations() ).isEqualTo( 6 );
		assertThat( notifier.getAllListeners() ).as( "all listeners are unregistered" ).isEmpty();

		// check ignore notifications  
		assertThat( testedObserver.getObserver().getInvocations() ).isEmpty();
		assertThat( testedObserver.getPropertyChangeListener().getInvocations() ).isEmpty();
		assertThat( testedObserver.getMyListener().getInvocations() ).isEmpty();
		assertThat( testedObserver.getMyAnotherListener().getInvocations() ).isEmpty();
	}


	@Test
	void notifyObservable() {
		( (ObservableNotifier) notifier ).notifyObservers();
		( (ObservableNotifier) notifier ).notifyObservers( "v1" );
		( (Observer) notifier ).update( null, "v2" );
		notifier.notifierFor( Observer.class ).update( null, "v3" );

		assertThat( testedObserver.getObserver().getInvocations() )
				.extracting( InvocationDef::getParam )
				.containsExactly( null, "v1", "v2", "v3" );

		assertThat( testedObserver.getObserver().getInvocations() )
				.extracting( InvocationDef::getSource )
				.containsOnlyNulls();
	}

	@Test
	void observableNotifier_all_methods_throw_no_exception() {
		ObservableNotifier observableNotifier = (ObservableNotifier) this.notifier;

		observableNotifier.notifyObservers();
		observableNotifier.notifyObservers( "v" );
		assertThat( observableNotifier.countObservers() ).isEqualTo( 1 );
	}


	@SuppressWarnings( "unchecked" )
	@Test
	void notifyPropertyChangeSupport() {
		( (PropertyChangeNotifier) notifier ).firePropertyChange( "prop", "o1", "n1" );
		( (PropertyChangeNotifier) notifier ).firePropertyChange( null, "o2", "n2" );
		( (PropertyChangeListener) notifier ).propertyChange( new PropertyChangeEvent( mockedObservable, "p3", "o3", "n3" ) );
		notifier.notifierFor( PropertyChangeListener.class ).propertyChange( new PropertyChangeEvent( mockedObservable, "p4", "o4", "n4" ) );
		notifier.notifierFor( "prop", PropertyChangeListener.class ).propertyChange( new PropertyChangeEvent( mockedObservable, "p5", "o5", "n5" ) );

		assertThat( testedObserver.getPropertyChangeListener().getInvocations() )
				.extracting( InvocationDef::getParam )
				.map( PropertyChangeEvent.class::cast )
				.extracting( PropertyChangeEvent::getSource,
				             PropertyChangeEvent::getPropertyName,
				             PropertyChangeEvent::getOldValue,
				             PropertyChangeEvent::getNewValue )
				.containsExactly(
						tuple( mockedObservable, "prop", "o1", "n1" ), // 1. time common
						tuple( mockedObservable, "prop", "o1", "n1" ), // 2. time named
						tuple( mockedObservable, null, "o2", "n2" ), // only common
						tuple( mockedObservable, "p3", "o3", "n3" ), // direct to listener
						tuple( mockedObservable, "p4", "o4", "n4" ), // direct to listener
						tuple( mockedObservable, "p5", "o5", "n5" ) // direct to listener
				);
	}

	@Test
	void propertyChangeNotifier_all_methods_throw_no_exception() {
		PropertyChangeNotifier propertyChangeNotifier = (PropertyChangeNotifier) this.notifier;

		propertyChangeNotifier.firePropertyChange( "prop", "old", "new" );
		propertyChangeNotifier.firePropertyChange( "prop", 1, 2 );
		propertyChangeNotifier.firePropertyChange( "prop", true, false );
		propertyChangeNotifier.firePropertyChange( new PropertyChangeEvent( mockedObservable, "prop", "o", "n" ) );
		propertyChangeNotifier.fireIndexedPropertyChange( "prop", 0, "old", "new" );
		propertyChangeNotifier.fireIndexedPropertyChange( "prop", 1, 2, 3 );
		propertyChangeNotifier.fireIndexedPropertyChange( "prop", 2, true, false );

		assertThat( propertyChangeNotifier.hasListeners( "prop" ) ).isTrue();
		assertThat( propertyChangeNotifier.hasListeners( null ) ).isTrue();
		assertThat( propertyChangeNotifier.getPropertyChangeListeners() ).hasSize( 2 ).containsOnly( testedObserver.getPropertyChangeListener() );
		assertThat( propertyChangeNotifier.getPropertyChangeListeners( "prop" ) ).containsExactly( testedObserver.getPropertyChangeListener() );
		assertThat( propertyChangeNotifier.getPropertyChangeListeners( "unknown" ) ).isEmpty();
	}


	@Test
	void notifyMyListener() {
		( (MyListener) notifier ).somethingChanged1( "v1" );
		notifier.notifierFor( MyListener.class ).somethingChanged2( "v2" );
		notifier.notifierFor( MyListener.class, selector( "presel", "postsel" ) ).somethingChanged1( "v3" );

		assertThat( testedObserver.getMyListener().getInvocations() )
				.extracting( InvocationDef::getMethod, InvocationDef::getParam )
				.containsExactly(
						tuple( "somethingChanged1", "v1" ),
						tuple( "somethingChanged2", "v2" ),
						tuple( "somethingChanged1", "v3" )
				);
	}

	@Test
	void notify_no_listener_strict() {
		ListenerSelector unknownSelector = selector( "unknown" );
		assertThatThrownBy( () -> notifier.notifierFor( PropertyChangeListener.class, unknownSelector ) ).isInstanceOf( MockoborException.class );
	}

	@Test
	void notify_no_listener_lenient() {
		notifier.setStrictCheckListenerList( false );
		ListenerSelector unknownSelector = selector( "unknown" );
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( PropertyChangeListener.class, unknownSelector ) );
	}
}

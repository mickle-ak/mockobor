package org.mockobor.mockedobservable;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.Mockobor;

import static org.assertj.core.api.Assertions.*;


class UsageExample_allListenersAreUnregistered_Test {

	@Test
	void assertThatAllListenersAreUnregistered_OK() {
		MockedObservable mockedObservable1 = Mockito.mock( MockedObservable.class, "mockedObservable1" );
		MockedObservable mockedObservable2 = Mockito.mock( MockedObservable.class, "mockedObservable2" );
		MockedObservable mockedObservable3 = Mockito.mock( MockedObservable.class, "mockedObservable3" );

		ListenersNotifier notifier1 = Mockobor.createNotifierFor( mockedObservable1 );
		ListenersNotifier notifier2 = Mockobor.createNotifierFor( mockedObservable2 );
		ListenersNotifier notifier3 = Mockobor.createNotifierFor( mockedObservable3 );

		TestedObserver testedObserver = new TestedObserver( mockedObservable1, mockedObservable2, mockedObservable3 );
		testedObserver.destroy();

		assertThatNoException().isThrownBy(
				() -> Mockobor.assertThatAllListenersAreUnregistered( notifier1, notifier2, notifier3 ) );

		assertThat( notifier1.allListenersAreUnregistered() ).isTrue();
		assertThat( notifier2.allListenersAreUnregistered() ).isTrue();
		assertThat( notifier3.allListenersAreUnregistered() ).isTrue();
	}


	@SuppressWarnings( "unused" )
	@Test
	void assertThatAllListenersAreUnregistered_Exception() {
		MockedObservable mockedObservable1 = Mockito.mock( MockedObservable.class, "mockedObservable1" );
		MockedObservable mockedObservable2 = Mockito.mock( MockedObservable.class, "mockedObservable2" );
		MockedObservable mockedObservable3 = Mockito.mock( MockedObservable.class, "mockedObservable3" );

		ListenersNotifier notifier1 = Mockobor.createNotifierFor( mockedObservable1 );
		ListenersNotifier notifier2 = Mockobor.createNotifierFor( mockedObservable2 );
		ListenersNotifier notifier3 = Mockobor.createNotifierFor( mockedObservable3 );

		TestedObserver testedObserver12 = new TestedObserver( mockedObservable1, mockedObservable2 );
		TestedObserver testedObserver3 = new TestedObserver( mockedObservable3 ); // register, but no destroying
		testedObserver12.destroy();

		assertThat( notifier1.allListenersAreUnregistered() ).isTrue();
		assertThat( notifier2.allListenersAreUnregistered() ).isTrue();
		assertThat( notifier3.allListenersAreUnregistered() ).isFalse();

		assertThatThrownBy( () -> Mockobor.assertThatAllListenersAreUnregistered( notifier1, notifier2, notifier3 ) )
				.hasMessageContaining( "mockedObservable3" ) // the mocked object with not unregistered listeners
				.hasMessageContainingAll( "MyListener", "MyAnotherListener", "PropertyChangeListener", "Observer" )// not unregistered listeners classes
				.hasMessageContainingAll( "prop", "presel", "postsel" ); // not unregistered selectors
	}
}

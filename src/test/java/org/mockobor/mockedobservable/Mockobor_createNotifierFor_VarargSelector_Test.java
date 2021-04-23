package org.mockobor.mockedobservable;

import lombok.Getter;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.Mockobor;
import org.mockobor.mockedobservable.MockedObservable.MyListener;
import org.mockobor.mockedobservable.TestedObserver.InvocationDef;
import org.mockobor.mockedobservable.TestedObserver.MyListenerImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class Mockobor_createNotifierFor_VarargSelector_Test {

	public interface VarargObservable {
		void addListener( MyListener listener, String... multiSelector );
		void removeListener( MyListener listener, String... multiSelector );
	}

	public static class VarargObserver {
		@Getter private final MyListenerImpl   listener0 = new MyListenerImpl();
		@Getter private final MyListenerImpl   listener1 = new MyListenerImpl();
		@Getter private final MyListenerImpl   listener2 = new MyListenerImpl();
		private final         VarargObservable observable;

		public VarargObserver( VarargObservable observable ) {
			this.observable = observable;
			this.observable.addListener( listener0 );
			this.observable.addListener( listener1, "s1" );
			this.observable.addListener( listener2, "s1", "s2" );
		}

		public void dispose() {
			observable.removeListener( listener0 );
			observable.removeListener( listener1, "s1" );
			observable.removeListener( listener2, "s1", "s2" );
		}
	}


	// ==================================================================================
	// =================================== Mockito ======================================
	// ==================================================================================

	@Test
	void create_vararg_Mockito() {
		VarargObservable observable = Mockito.mock( VarargObservable.class );
		ListenersNotifier notifier = Mockobor.createNotifierFor( observable );
		VarargObserver observer = new VarargObserver( observable );

		notifier.notifierFor( MyListener.class, selector() ).somethingChanged1( "selector()" );
		notifier.notifierFor( MyListener.class, selector( "s1" ) ).somethingChanged1( "selector(s1)" );
		notifier.notifierFor( MyListener.class, selector( "s1", "s2" ) ).somethingChanged1( "selector(s1, s2)" );

		assertThat( observer.getListener0().getInvocations() )
				.extracting( InvocationDef::getParam )
				.containsExactly( "selector()" );

		assertThat( observer.getListener1().getInvocations() )
				.extracting( InvocationDef::getParam )
				.containsExactly( "selector(s1)" );

		assertThat( observer.getListener2().getInvocations() )
				.extracting( InvocationDef::getParam )
				.containsExactly( "selector(s1, s2)" );
	}

	@Test
	void deregistration_vararg_Mockito() {
		VarargObservable observable = Mockito.mock( VarargObservable.class );
		ListenersNotifier notifier = Mockobor.createNotifierFor( observable );
		VarargObserver observer = new VarargObserver( observable );
		observer.dispose();

		assertThat( notifier.allListenersAreUnregistered() ).isTrue();
	}


	// ==================================================================================
	// =================================== EasyMock ======================================
	// ==================================================================================

	@Test
	void create_vararg_EasyMock() {
		VarargObservable observable = EasyMock.mock( VarargObservable.class );
		EasyMock.replay( observable );
		assertThatThrownBy( () -> new VarargObserver( observable ) )
				.as( "EasyMock does not allow to create 'common' stub for vararg methods" )
				.hasMessageContaining( "VarargObservable.addListener" );
	}
}

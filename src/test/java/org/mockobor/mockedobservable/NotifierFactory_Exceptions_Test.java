package org.mockobor.mockedobservable;

import org.junit.jupiter.api.Test;
import org.mockobor.exceptions.ListenerRegistrationMethodsNotDetectedException;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.listener_detectors.ListenerDetectorsRegistryImpl;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistryImpl;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


class NotifierFactory_Exceptions_Test {

	public interface WithoutAddListener {}

	public interface OnlyRemoveMethod {

		@SuppressWarnings( "unused" )
		void removeMyListener( MockedObservable.MyListener listener );
	}

	public interface OnlyAddMethod {

		@SuppressWarnings( "unused" )
		void addMyListener( MockedObservable.MyListener listener );
	}

	public static class OnlyAddMethodsObject implements OnlyAddMethod {

		@Override
		public void addMyListener( MockedObservable.MyListener listener ) {}
	}


	// ==================================================================================
	// ==================================== tests =======================================
	// ==================================================================================

	private final NotifierFactory factory = new NotifierFactory( new ListenerDetectorsRegistryImpl(),
	                                                             new MockingToolsRegistryImpl() );


	@Test
	void no_listener_registration_methods_detected() {

		assertThatThrownBy( () -> factory.create( mock( WithoutAddListener.class ) ) )
				.isInstanceOf( ListenerRegistrationMethodsNotDetectedException.class );

		assertThatThrownBy( () -> factory.create( mock( OnlyRemoveMethod.class ) ) )
				.isInstanceOf( ListenerRegistrationMethodsNotDetectedException.class );

		assertThatNoException().isThrownBy( () -> factory.create( mock( OnlyAddMethod.class ) ) );
	}

	@SuppressWarnings( "java:S5778" )
	@Test
	void no_mock() {
		assertThatThrownBy( () -> factory.create( new OnlyAddMethodsObject() ) )
				.isInstanceOf( MockingToolNotDetectedException.class );

		assertThatNoException().isThrownBy( () -> factory.create( mock( OnlyAddMethodsObject.class ) ) );
		assertThatNoException().isThrownBy( () -> factory.create( spy( new OnlyAddMethodsObject() ) ) );
	}
}

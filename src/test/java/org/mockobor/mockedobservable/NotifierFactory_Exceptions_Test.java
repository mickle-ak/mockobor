package org.mockobor.mockedobservable;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.exceptions.ListenerRegistrationMethodsNotDetectedException;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.listener_detectors.ListenerDetectorsRegistryImpl;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistryImpl;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


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

	private final NotifierSettings defaultSettings = NotifierSettingsImpl.createDefaultSettings();

	private final NotifierFactory factory = new NotifierFactory( new ListenerDetectorsRegistryImpl(),
	                                                             new MockingToolsRegistryImpl() );


	@Test
	void no_listener_registration_methods_detected_Mockito() {

		assertThatThrownBy( () -> factory.create( Mockito.mock( WithoutAddListener.class ), defaultSettings ) )
				.isInstanceOf( ListenerRegistrationMethodsNotDetectedException.class );

		assertThatThrownBy( () -> factory.create( Mockito.mock( OnlyRemoveMethod.class ), defaultSettings ) )
				.isInstanceOf( ListenerRegistrationMethodsNotDetectedException.class );

		assertThatNoException().isThrownBy( () -> factory.create( Mockito.mock( OnlyAddMethod.class ), defaultSettings ) );
	}

	@SuppressWarnings( "java:S5778" )
	@Test
	void no_listener_registration_methods_detected_EasyMock() {

		assertThatThrownBy( () -> factory.create( EasyMock.mock( WithoutAddListener.class ), defaultSettings ) )
				.isInstanceOf( ListenerRegistrationMethodsNotDetectedException.class );

		assertThatThrownBy( () -> factory.create( EasyMock.mock( OnlyRemoveMethod.class ), defaultSettings ) )
				.isInstanceOf( ListenerRegistrationMethodsNotDetectedException.class );

		assertThatNoException().isThrownBy( () -> factory.create( EasyMock.mock( OnlyAddMethod.class ), defaultSettings ) );
	}

	@SuppressWarnings( "java:S5778" )
	@Test
	void no_mock() {
		assertThatThrownBy( () -> factory.create( new OnlyAddMethodsObject(), defaultSettings ) )
				.isInstanceOf( MockingToolNotDetectedException.class );
	}
}

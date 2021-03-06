package org.mockobor.mockedobservable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockobor.Mockobor;
import org.mockobor.MockoborContext;
import org.mockobor.exceptions.ListenersNotFoundException;
import org.mockobor.listener_detectors.ListenerSelector;
import org.mockobor.mockedobservable.TestedObserver.InvocationDef;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observer;

import static org.assertj.core.api.Assertions.*;
import static org.mockobor.listener_detectors.ListenerSelector.selector;


class UsageExample_NotifierSettings_Test {

	private final MockedObservable mockedObservable = Mockito.mock( MockedObservable.class );
	private final TestedObserver   testedObserver   = new TestedObserver( mockedObservable );

	@AfterEach
	void tearDown() {
		// reset context to avoid correlation with other tests
		MockoborContext.reset();
	}


	// ==================================================================================
	// ======================== implement listener interfaces ===========================
	// ==================================================================================

	@Test
	void doNotImplementListenerInterfaces_using_MockoborContext() {
		// set global flag to ignore listener interfaces
		MockoborContext.updateNotifierSettings().ignoreListenersInterfaces();

		// create notifier with global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable );

		// notifier does not implement listener interfaces
		assertThat( notifier ).isNotInstanceOf( MockedObservable.MyListener.class )
		                      .isNotInstanceOf( MockedObservable.MyAnotherListener.class )
		                      .isNotInstanceOf( PropertyChangeListener.class )
		                      .isNotInstanceOf( Observer.class );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}

	@Test
	void doNotImplementListenerInterfaces_using_local_settings() {
		// set global flag to implement listener interfaces (it is default, for better readability only)
		MockoborContext.updateNotifierSettings().implementListenersInterfaces();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().ignoreListenersInterfaces() );

		// notifier does not implement listener interfaces
		assertThat( notifier ).isNotInstanceOf( MockedObservable.MyListener.class )
		                      .isNotInstanceOf( MockedObservable.MyAnotherListener.class )
		                      .isNotInstanceOf( PropertyChangeListener.class )
		                      .isNotInstanceOf( Observer.class );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}

	@Test
	void implementInterfaces_using_local_settings() {
		// set global flag to ignore listener interfaces
		MockoborContext.updateNotifierSettings().ignoreListenersInterfaces();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().implementListenersInterfaces() );

		// notifier implements listener interfaces
		assertThat( notifier ).isInstanceOf( MockedObservable.MyListener.class )
		                      .isInstanceOf( MockedObservable.MyAnotherListener.class )
		                      .isInstanceOf( PropertyChangeListener.class )
		                      .isInstanceOf( Observer.class );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}


	// ==================================================================================
	// ===================== strict check listener list to notifier =====================
	// ==================================================================================

	@Test
	void lenientCheckListenersList_using_MockoborContext() {
		// set global flag to lenient check of listeners list by notify
		MockoborContext.updateNotifierSettings().lenientListenerListCheck();

		// create notifier with global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable );

		// no exception on try to notify listener with unknown selector
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( MockedObservable.MyListener.class, selector( "unknown" ) ) );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}

	@Test
	void lenientCheckListenersList_using_local_settings() {
		// set global flag to strict check of listeners list by notify (it is default, for better readability only)
		MockoborContext.updateNotifierSettings().strickListenerListCheck();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().lenientListenerListCheck() );

		// no exception on try to notify listener with unknown selector
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( MockedObservable.MyListener.class, selector( "unknown" ) ) );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}

	@Test
	void strictCheckListenersList_using_local_settings() {
		// set global flag to lenient check of listeners list by notify
		MockoborContext.updateNotifierSettings().lenientListenerListCheck();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().strickListenerListCheck() );

		// exception on try to notify listener with unknown selector
		ListenerSelector unknownSelector = selector( "unknowing" );
		assertThatThrownBy( () -> notifier.notifierFor( MockedObservable.MyListener.class, unknownSelector ) )
				.isInstanceOf( ListenersNotFoundException.class );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}


	// ==================================================================================
	// =================================== common =======================================
	// ==================================================================================
	@Test
	void local_settings_does_not_change_global_settings() {
		// set global settings
		MockoborContext.updateNotifierSettings().lenientListenerListCheck().ignoreListenersInterfaces();

		// create first notifier with local settings
		ListenersNotifier localSettingsNotifier = Mockobor.createNotifierFor( mockedObservable,
		                                                                      Mockobor.notifierSettings()
		                                                                              .strickListenerListCheck()
		                                                                              .implementListenersInterfaces() );

		// create second notifier with global settings (lenientListenerListCheck + ignoreListenersInterfaces())
		ListenersNotifier globalSettingsNotifier = Mockobor.createNotifierFor( mockedObservable );

		// no exception on try to notify listener with unknown selector
		assertThatNoException().isThrownBy( () -> globalSettingsNotifier.notifierFor( MockedObservable.MyListener.class, selector( "unknown" ) ) );

		// notifier does not implement listener interfaces
		assertThat( globalSettingsNotifier ).isNotInstanceOf( MockedObservable.MyListener.class )
		                                    .isNotInstanceOf( MockedObservable.MyAnotherListener.class )
		                                    .isNotInstanceOf( PropertyChangeListener.class )
		                                    .isNotInstanceOf( Observer.class );
	}


	// ==================================================================================
	// =================================== helpers ======================================
	// ==================================================================================

	private void assertThatNotifierIsCorrectAndLinkedToTestedObject( ListenersNotifier notifier ) {
		( (PropertyChangeNotifier) notifier ).firePropertyChange( null, null, "v1" );
		assertThat( testedObserver.getPropertyChangeListener().getInvocations() )
				.extracting( InvocationDef::getParam )
				.map( PropertyChangeEvent.class::cast )
				.extracting( PropertyChangeEvent::getNewValue )
				.containsExactly( "v1" );
	}


}

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
		MockoborContext.updateNotifierSettings().ignoreListenerInterfaces();

		// create notifier with global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable );

		// notifier does not implement listener interfaces
		//noinspection deprecation
		assertThat( notifier ).isNotInstanceOf( MockedObservable.MyListener.class )
		                      .isNotInstanceOf( MockedObservable.MyAnotherListener.class )
		                      .isNotInstanceOf( PropertyChangeListener.class )
		                      .isNotInstanceOf( Observer.class );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}

	@Test
	void doNotImplementListenerInterfaces_using_local_settings() {
		// set the global flag to implement listener interfaces (it is the default, for better readability only)
		MockoborContext.updateNotifierSettings().implementListenerInterfaces();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().ignoreListenerInterfaces() );

		// notifier does not implement listener interfaces
		//noinspection deprecation
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
		MockoborContext.updateNotifierSettings().ignoreListenerInterfaces();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().implementListenerInterfaces() );

		// notifier implements listener interfaces
		//noinspection deprecation
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
	void lenientCheckListenerList_using_MockoborContext() {
		// set the global flag to lenient check of the listener list by notify
		MockoborContext.updateNotifierSettings().lenientListenerListCheck();

		// create notifier with global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable );

		// no exception on try to notify listener with unknown selector
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( MockedObservable.MyListener.class, selector( "unknown" ) ) );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}

	@Test
	void lenientCheckListenerList_using_local_settings() {
		// set the global flag to strict check of ths listener list by notify (it is the default, for better readability only)
		MockoborContext.updateNotifierSettings().strictListenerListCheck();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().lenientListenerListCheck() );

		// no exception on try to notify listener with unknown selector
		assertThatNoException().isThrownBy( () -> notifier.notifierFor( MockedObservable.MyListener.class, selector( "unknown" ) ) );

		// notifier is correct
		assertThatNotifierIsCorrectAndLinkedToTestedObject( notifier );
	}

	@Test
	void strictCheckListenerList_using_local_settings() {
		// set the global flag to lenient check of the listener list by notify
		MockoborContext.updateNotifierSettings().lenientListenerListCheck();

		// create notifier with local setting, it overrides global settings
		ListenersNotifier notifier = Mockobor.createNotifierFor( mockedObservable,
		                                                         Mockobor.notifierSettings().strictListenerListCheck() );

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
		MockoborContext.updateNotifierSettings().lenientListenerListCheck().ignoreListenerInterfaces();

		// create first notifier with local settings
		@SuppressWarnings("unused")
		ListenersNotifier localSettingsNotifier = Mockobor.createNotifierFor( mockedObservable,
		                                                                      Mockobor.notifierSettings()
		                                                                              .strictListenerListCheck()
		                                                                              .implementListenerInterfaces() );

		// create second notifier with global settings (lenientListenerListCheck + ignoreListenerInterfaces())
		ListenersNotifier globalSettingsNotifier = Mockobor.createNotifierFor( mockedObservable );

		// no exception on try to notify listener with unknown selector
		assertThatNoException().isThrownBy( () -> globalSettingsNotifier.notifierFor( MockedObservable.MyListener.class, selector( "unknown" ) ) );

		// notifier does not implement listener interfaces
		//noinspection deprecation
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

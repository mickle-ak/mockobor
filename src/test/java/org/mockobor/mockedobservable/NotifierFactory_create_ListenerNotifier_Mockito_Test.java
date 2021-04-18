package org.mockobor.mockedobservable;


import org.mockito.Mockito;


class NotifierFactory_create_ListenerNotifier_Mockito_Test extends NotifierFactory_create_ListenerNotifier_TestBase {

	@Override
	protected MockedObservable createMock() {
		return Mockito.mock( MockedObservable.class );
	}

	@Override
	protected void endOfStubbingMode( Object mock ) {
		// Mockito don't need to change mode
	}
}

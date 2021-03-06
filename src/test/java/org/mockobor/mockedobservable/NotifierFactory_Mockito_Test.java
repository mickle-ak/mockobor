package org.mockobor.mockedobservable;


import org.mockito.Mockito;


class NotifierFactory_Mockito_Test extends NotifierFactory_TestBase {

	@Override
	protected MockedObservable createMock() {
		return Mockito.mock( MockedObservable.class );
	}

	@Override
	protected void endOfStubbingMode( Object mock ) {
		// Mockito don't need to change mode
	}
}

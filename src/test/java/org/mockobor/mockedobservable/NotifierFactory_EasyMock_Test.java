package org.mockobor.mockedobservable;


import org.easymock.EasyMock;


class NotifierFactory_EasyMock_Test extends NotifierFactory_TestBase {

	@Override
	protected MockedObservable createMock() {
		return EasyMock.mock( MockedObservable.class );
	}

	@Override
	protected void endOfStubbingMode( Object mock ) {
		EasyMock.replay( mock );
	}
}

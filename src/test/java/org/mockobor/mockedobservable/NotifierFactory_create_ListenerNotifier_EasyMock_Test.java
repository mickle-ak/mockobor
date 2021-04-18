package org.mockobor.mockedobservable;


import org.easymock.EasyMock;


class NotifierFactory_create_ListenerNotifier_EasyMock_Test extends NotifierFactory_create_ListenerNotifier_TestBase {

	@Override
	protected MockedObservable createMock() {
		return EasyMock.mock( MockedObservable.class );
	}

	@Override
	protected void endOfStubbingMode( Object mock ) {
		EasyMock.replay( mock );
	}
}

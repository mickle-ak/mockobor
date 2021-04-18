package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import org.mockito.Mockito;


class MockitoListenerRegistrationHandlerTest extends ListenerRegistrationHandler_TestBase {

	@Override
	protected TestMethods createMock() {
		return Mockito.mock( TestMethods.class );
	}

	@Override
	protected MockitoListenerRegistrationHandler createListenerRegistrationHandler() {
		return new MockitoListenerRegistrationHandler();
	}

	@Override
	protected Object createPartialMock( @NonNull Object object ) {
		return Mockito.spy( object );
	}

	@Override
	protected void endOfStubbingMode( Object mock ) {
		// Mockito don't need to change mode
	}
}

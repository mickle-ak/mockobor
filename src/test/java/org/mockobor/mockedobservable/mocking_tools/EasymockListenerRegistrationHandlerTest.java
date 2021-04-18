package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Disabled;


class EasymockListenerRegistrationHandlerTest extends ListenerRegistrationHandler_TestBase {

	@Override
	protected TestMethods createMock() {
		return EasyMock.mock( TestMethods.class );
	}

	@Override
	protected ListenerRegistrationHandler createListenerRegistrationHandler() {
		return new EasymockListenerRegistrationHandler();
	}

	@Override
	protected Object createPartialMock( @NonNull Object object ) {
		return EasyMock.partialMockBuilder( object.getClass() ).createMock();
	}

	@Override
	protected void endOfStubbingMode( Object mock ) {
		EasyMock.replay( mock );
	}


	@Disabled( "EasyMock can not correct match vararg methods " )
	@Override
	void registerInMock_varargsObject() {
		super.registerInMock_varargsObject();
	}

	@Disabled( "EasyMock can not correct match vararg methods " )
	@Override
	void registerInMock_varargsInt() {
		super.registerInMock_varargsInt();
	}
}

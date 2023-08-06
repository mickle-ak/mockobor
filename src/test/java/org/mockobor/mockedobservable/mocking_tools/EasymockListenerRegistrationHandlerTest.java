package org.mockobor.mockedobservable.mocking_tools;

import org.easymock.EasyMock;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


@SuppressWarnings( { "EmptyMethod", "unused" } )
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


	// ==================================================================================
	// ========================= EasyMock specific tests ================================
	// ==================================================================================

	@Test
	@Disabled( "EasyMock can not correctly match vararg methods" )
	void registerInMock_varargsObject() {} // NOSONAR: disabled => no assertions

	@Test
	@Disabled( "EasyMock can not correctly match vararg methods" )
	void registerInMock_varargsInt() {} // NOSONAR: disabled => no assertions
}

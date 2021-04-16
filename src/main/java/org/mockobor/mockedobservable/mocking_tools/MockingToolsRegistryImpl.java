package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.exceptions.MockoborIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;


public class MockingToolsRegistryImpl implements MockingToolsRegistry {

	private final List<ListenerRegistrationHandler> availableHandlers = new ArrayList<>();

	public MockingToolsRegistryImpl() {
		registerDefaultMockingTools();
	}

	@Override
	public @NonNull ListenerRegistrationHandler findHandlerForMock( @NonNull Object mockedObservable ) {
		for( ListenerRegistrationHandler h : availableHandlers ) {
			if( h.canHandle( mockedObservable ) ) return h;
		}
		throw new MockingToolNotDetectedException( mockedObservable );
	}


	@Override
	public boolean registerMockingTool( @NonNull String mockingToolDetectClassName, @NonNull String registrationHandlerClassName )
			throws MockoborIllegalArgumentException {
		try {
			// try to find mocking tool class 
			Class.forName( mockingToolDetectClassName, false, Thread.currentThread().getContextClassLoader() );
		}
		catch( ClassNotFoundException e ) {
			return false; // mocking tool not available in classpath
		}

		try {
			// create and add registration handler
			Object registrationHandle = Class.forName( registrationHandlerClassName ).newInstance();
			if( !( registrationHandle instanceof ListenerRegistrationHandler ) ) {
				throw new MockoborIllegalArgumentException( "Registration handle (%s) must implement ListenerRegistrationHandler",
				                                            registrationHandlerClassName );
			}
			availableHandlers.add( (ListenerRegistrationHandler) registrationHandle );
			return true;
		}
		catch( InstantiationException | IllegalAccessException | ClassNotFoundException e ) {
			throw new MockoborIllegalArgumentException( "Can not create registration handle (%s)", registrationHandlerClassName );
		}
	}


	@Override
	public void reset() {
		availableHandlers.clear();
		registerDefaultMockingTools();
	}

	private void registerDefaultMockingTools() {
		registerMockingTool( "org.mockito.Mockito",
		                     "org.mockobor.mockedobservable.mocking_tools.MockitoListenerRegistrationHandler" );
	}
}

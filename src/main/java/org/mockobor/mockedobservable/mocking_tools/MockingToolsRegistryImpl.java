package org.mockobor.mockedobservable.mocking_tools;

import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.jdt.annotation.NonNull;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.exceptions.MockoborIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of {@link MockingToolsRegistry}.
 */
public class MockingToolsRegistryImpl implements MockingToolsRegistry {

	@Getter( AccessLevel.PACKAGE )
	private final List<ListenerRegistrationHandler> availableHandlers = new ArrayList<>();

	public MockingToolsRegistryImpl() {
		registerDefaultMockingTools();
	}


	@Override
	public @NonNull ListenerRegistrationHandler findHandlerForMock( @NonNull Object mockedObservable ) {
		for( ListenerRegistrationHandler h : availableHandlers ) {
			if( h.canHandle( mockedObservable ) ) {
				return h;
			}
		}
		throw new MockingToolNotDetectedException( mockedObservable );
	}


	@Override
	public boolean registerListenerRegistrationHandler( @NonNull ListenerRegistrationHandler registrationHandler ) {
		return availableHandlers.add( registrationHandler );
	}


	@Override
	public void reset() {
		availableHandlers.clear();
		registerDefaultMockingTools();
	}


	/**
	 * To add support for one of default mocking tool.
	 * <p></p>
	 * It uses class names to avoid premature loading of implementation classes
	 * and thrown of {@code ClassNotFoundException} if supported mocking tool not in classpath.
	 *
	 * @param mockingToolDetectClassName   name of class used to detect if the mocking tool in classpath (the class can be loaded)
	 * @param registrationHandlerClassName name of class implemented {@link ListenerRegistrationHandler} for the mocking tool.
	 *                                     It must have a no argument constructor.
	 * @return true if the specified mocking tool was found in classpath and corresponding handler is registered
	 * @throws MockoborIllegalArgumentException if mocking tool found, but listener registration handler can not be created
	 */
	boolean addDefaultMockingTool( @NonNull String mockingToolDetectClassName, @NonNull String registrationHandlerClassName )
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

	/*
	 * It uses class names to avoid premature loading of implementation classes
	 * and thrown of {@code ClassNotFoundException} if supported mocking tool not in classpath.
	 */
	private void registerDefaultMockingTools() {
		addDefaultMockingTool( "org.mockito.Mockito",
		                       "org.mockobor.mockedobservable.mocking_tools.MockitoListenerRegistrationHandler" );
		addDefaultMockingTool( "org.easymock.EasyMock",
		                       "org.mockobor.mockedobservable.mocking_tools.EasymockListenerRegistrationHandler" );
	}
}

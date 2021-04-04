package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import lombok.Value;
import org.mockobor.exceptions.MockoborIllegalArgumentException;

import java.util.ArrayList;
import java.util.List;


public class MockingToolsRegistryImpl implements MockingToolsRegistry {

	private final List<MockingToolDefinition> mockingToolDefinitions = new ArrayList<>();

	public MockingToolsRegistryImpl() {
		registerDefaultMockingTools();
	}

	@Override
	public @NonNull ListenerRegistrationHandler findHandlerForMock( @NonNull Object mockedObservable ) {
		return new MockitoListenerRegistrationHandler(); // TODO: implement selection;
	}


	@Override
	public boolean registerMockingTool( @NonNull String mockingToolDetectClassName, @NonNull String registrationHandlerClassName )
			throws MockoborIllegalArgumentException {
		mockingToolDefinitions.add( new MockingToolDefinition( mockingToolDetectClassName, registrationHandlerClassName ) );
		return true;
	}

	@Override
	public void reset() {
		mockingToolDefinitions.clear();
		registerDefaultMockingTools();
	}

	private void registerDefaultMockingTools() {
		registerMockingTool( "org.mockito.Mockito",
		                     "org.mockobor.mockedobservable.mocking_tools.MockitoListenerRegistrationHandler" );
	}


	@Value
	private static class MockingToolDefinition {

		String mockingToolDetectClassName;
		String registrationHandlerClassName;
	}
}

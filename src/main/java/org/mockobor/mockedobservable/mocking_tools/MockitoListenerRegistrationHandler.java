package org.mockobor.mockedobservable.mocking_tools;


import lombok.NonNull;
import org.mockito.internal.util.MockUtil;
import org.mockobor.listener_detectors.ListenerContainer;
import org.mockobor.listener_detectors.RegistrationDelegate;


/**
 * Implementation of {@link ListenerRegistrationHandler} for Mockito.
 */
public class MockitoListenerRegistrationHandler implements ListenerRegistrationHandler {

	@Override
	public boolean canHandle( Object mockedObservable ) {
		return MockUtil.isMock( mockedObservable );
	}

	@Override
	public void registerInMock( @NonNull ListenerContainer listeners, @NonNull RegistrationDelegate registration ) {

	}
}

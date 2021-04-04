package org.mockobor.mockedobservable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.mockobor.Mockobor;
import org.mockobor.exceptions.ListenerRegistrationMethodsNotDetectedException;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.listener_detectors.ListenerDetectorsRegistry;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistry;


/**
 * Factory for notifier objects.
 * <p></p>
 * It searchs for registration (add/remove listener) methods for all possible observer/listeners,
 * detect used mocking tool,
 * redirect add/remove-listeners methods from mocked object to itself (using detected mocking tool) and
 * creates dynamic proxy as notifier object.
 * <p></p>
 * It used in {@link Mockobor#createNotifierFor} to do real work.
 */
@RequiredArgsConstructor
public class NotifierFactory {

	@NonNull
	private final ListenerDetectorsRegistry listenerDetectorsRegistry;

	@NonNull
	private final MockingToolsRegistry mockingToolsRegistry;


	/**
	 * To create notifier object for the specified mocked observable.
	 *
	 * @param mockedObservable mock of observable object
	 * @return notifier used to simulate notification calls from the specified mocked observable
	 * @throws ListenerRegistrationMethodsNotDetectedException if neither of listener definition detectors can detect listener registration methods
	 * @throws MockingToolNotDetectedException                 if the specified object not a mock or mocking tool used to mock it not supported
	 */
	@NonNull
	public ListenersNotifier create( @NonNull Object mockedObservable )
			throws ListenerRegistrationMethodsNotDetectedException, MockingToolNotDetectedException {
		return null;
	}
}

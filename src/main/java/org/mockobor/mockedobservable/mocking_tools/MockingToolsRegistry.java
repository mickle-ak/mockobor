package org.mockobor.mockedobservable.mocking_tools;

import org.eclipse.jdt.annotation.NonNull;
import org.mockobor.exceptions.MockingToolNotDetectedException;


/**
 * To manage {@link ListenerRegistrationHandler}s which are dependent on supported mocking tools.
 * <p><br>
 * Out of the box follow mocking tools are registered:<ul>
 * <li>Mockito: {@code MockitoListenerRegistrationHandler}</li>
 * <li>EasyMock: {@code EasymockListenerRegistrationHandler}</li>
 * </ul>
 * <p><br>
 * Usually you don't need to use this.
 * Only if you want to register custom implementation of {@link ListenerRegistrationHandler} for your mocking tool.
 */
public interface MockingToolsRegistry {


	/**
	 * To get {@code ListenerRegistrationHandler} for the specified mocked object.
	 * <p>
	 * It detects mocking tool used to mock the specified mocked object and
	 * returns the corresponding listener registration handler.
	 * <p>
	 * To detect used mocking tool it finds all mocking tools available in classpath
	 * (which "detect class" can be loaded with the current class loaded)
	 * and calls {@link ListenerRegistrationHandler#canHandle} method.
	 * <p>
	 * If handler can not be found, then it throws {@code MockingToolNotDetectedException}.
	 *
	 * @param mockedObservable mocked object to check
	 * @return registration handlers for the specified mocked observable available.
	 * @throws MockingToolNotDetectedException if handler con not be found
	 */
	@NonNull ListenerRegistrationHandler findHandlerForMock( @NonNull Object mockedObservable )
			throws MockingToolNotDetectedException;


	/**
	 * To add custom support for your mocking tool.
	 * <p></p>
	 * Usually you don't need to use this.
	 * Only if you want to register custom implementation of {@link ListenerRegistrationHandler} for your mocking tool.
	 *
	 * @param registrationHandler class implemented {@link ListenerRegistrationHandler} for your mocking tool.
	 */
	boolean registerListenerRegistrationHandler( @NonNull ListenerRegistrationHandler registrationHandler );


	/**
	 * To remove all registered custom support for your mocking tools.
	 * <p><br>
	 * Usually you don't need to call it.
	 * Only if you want to reset changes made by registration of custom support for your mocking tool.
	 */
	void reset();
}

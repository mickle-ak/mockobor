package org.mockobor.mockedobservable.mocking_tools;

import lombok.NonNull;
import org.mockobor.exceptions.MockingToolNotDetectedException;
import org.mockobor.exceptions.MockoborIllegalArgumentException;


/**
 * To manage {@link ListenerRegistrationHandler}s which are dependent on supported mocking tools.
 * <p><br>
 * Out of the box (per default) follow mocking tools are registered:<ul>
 * <li>Mockito -> {@code MockitoListenerRegistrationHandler}</li>
 * </ul>
 * <p><br>
 * Usually you don't need to use this.
 * Only if you want to register custom implementation of {@link ListenerRegistrationHandler} for your mocking tool.
 */
public interface MockingToolsRegistry {


	/**
	 * To get {@code ListenerRegistrationHandler} for the specified mocked object.
	 * <p>
	 * It detects mocking tool used to mock the specified mocked object and returns corresponding listener registration handler.
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
	 * It uses class names to avoid  premature loading and thrown of {@code ClassNotFoundException}
	 * if supported mocking tool not in classpath.
	 * <p></p>
	 * Usually you don't need to use this.
	 * Only if you want to register custom implementation of {@link ListenerRegistrationHandler} for your mocking tool.
	 *
	 * @param mockingToolDetectClassName   name of class used to detect if the mocking tool in classpath (the class can be loaded)
	 * @param registrationHandlerClassName name of class implemented {@link ListenerRegistrationHandler} for the mocking tool.
	 * @return true if the specified mocking tool was found in classpath and corresponding handler is registered
	 * @throws MockoborIllegalArgumentException if mocking tool found, but listener registration handler can not be created
	 */
	boolean registerMockingTool( @NonNull String mockingToolDetectClassName, @NonNull String registrationHandlerClassName )
			throws MockoborIllegalArgumentException;


	/**
	 * To remove all registered custom support for your mocking tools.
	 * <p><br>
	 * Usually you don't need to call it.
	 * Only if you want to reset changes made by registration of custom support for your mocking tool.
	 */
	void reset();
}

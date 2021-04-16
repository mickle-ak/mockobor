package org.mockobor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.mockobor.listener_detectors.ListenerDefinitionDetector;
import org.mockobor.listener_detectors.ListenerDetectorsRegistry;
import org.mockobor.listener_detectors.ListenerDetectorsRegistryImpl;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistry;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistryImpl;


/**
 * Mockobor static context.
 * <p><br>
 * Usually you don't need to use it. Only if you want to <ul>
 * <li>register custom listener definition detectors</li>
 * <li>register custom mocking tool detectors</li>
 * </ul>
 * <p></p>
 * Important: Registered detectors are kept statically so it will stick between your unit tests.
 * Make sure you reset it if needed (using {@link #reset()} method).
 *
 * @see ListenerDetectorsRegistry
 */
@SuppressWarnings( "unused" )
@NoArgsConstructor( access = AccessLevel.PRIVATE )
public class MockoborContext {

	/** Listener definition registry used in static context. */
	static final ListenerDetectorsRegistry LISTENER_DETECTORS_REGISTRY = new ListenerDetectorsRegistryImpl();

	/** Mocking tool registry used in static context. */
	static final MockingToolsRegistry MOCKING_TOOLS_REGISTRY = new MockingToolsRegistryImpl();


	/**
	 * To add custom listener definition detector to static context.
	 * <p></p>
	 * Usually you don't need to call it. Only if you want to register custom listener definition detectors.
	 * <p></p>
	 * Important: Registered detectors are kept statically so it will stick between your unit tests.
	 * Make sure you reset it if needed.
	 *
	 * @param listenerDefinitionDetector custom listener definition detector
	 * @see ListenerDetectorsRegistry#registerListenerDefinitionDetector
	 */
	public static void registerListenerDefinitionDetector( ListenerDefinitionDetector listenerDefinitionDetector ) {
		LISTENER_DETECTORS_REGISTRY.registerListenerDefinitionDetector( listenerDefinitionDetector );
	}

	/**
	 * To add custom support for your mocking tool.
	 * <p><br>
	 * Usually you don't need to use this.
	 * Only if you want to register custom implementation of {@link ListenerRegistrationHandler} for your mocking tool.
	 *
	 * @param mockingToolClassName         name of class used to detect if the mocking tool in classpath (the class can be loaded)
	 * @param registrationHandlerClassName name of class implemented {@link ListenerRegistrationHandler} for the mocking tool.
	 */
	void registerMockingTool( @NonNull String mockingToolClassName, @NonNull String registrationHandlerClassName ) {
		MOCKING_TOOLS_REGISTRY.registerMockingTool( mockingToolClassName, registrationHandlerClassName );
	}


	/**
	 * To remove all registered custom listener definition detectors.
	 * <p><br>
	 * Usually you don't need to call it.
	 * Only if you want to reset changes made by registration of custom detectors or mocking tools.
	 *
	 * @see ListenerDetectorsRegistry#reset()
	 */
	public static void reset() {
		LISTENER_DETECTORS_REGISTRY.reset();
		MOCKING_TOOLS_REGISTRY.reset();
	}
}

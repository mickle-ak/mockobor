package org.mockobor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.jdt.annotation.NonNull;
import org.mockobor.listener_detectors.ListenerDefinitionDetector;
import org.mockobor.listener_detectors.ListenerDetectorsRegistry;
import org.mockobor.listener_detectors.ListenerDetectorsRegistryImpl;
import org.mockobor.mockedobservable.NotifierSettings;
import org.mockobor.mockedobservable.NotifierSettingsImpl;
import org.mockobor.mockedobservable.NotifierSettingsUpdater;
import org.mockobor.mockedobservable.mocking_tools.ListenerRegistrationHandler;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistry;
import org.mockobor.mockedobservable.mocking_tools.MockingToolsRegistryImpl;


/**
 * Mockobor static context.
 * <p><br>
 * Usually you don't need to use it. Only if you want to <ul>
 * <li>register custom listener definition detectors, use {@link #registerListenerDefinitionDetector}</li>
 * <li>register custom mocking tool detectors, use {@link #registerListenerRegistrationHandler}</li>
 * <li>change global settings, use {@link #updateNotifierSettings()}</li>
 * </ul>
 * <p></p>
 * Important: Registered detectors are kept statically, so it will stick between your unit tests.
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

	/** Settings to create a new notifier used in static context. */
	@NonNull
	static NotifierSettingsImpl notifierSettingsImpl = NotifierSettingsImpl.createDefaultSettings();


	/**
	 * To add custom listener definition detector to static context.
	 * <p></p>
	 * Usually you don't need to call it. Only if you want to register custom listener definition detectors.
	 * <p></p>
	 * Important: Registered detectors are kept statically, so it will stick between your unit tests.
	 * Make sure you reset it if needed (see {@link #reset()}).
	 *
	 * @param listenerDefinitionDetector custom listener definition detector
	 * @see ListenerDetectorsRegistry#registerListenerDefinitionDetector
	 * @see #reset()
	 */
	public static void registerListenerDefinitionDetector( @NonNull ListenerDefinitionDetector listenerDefinitionDetector ) {
		LISTENER_DETECTORS_REGISTRY.registerListenerDefinitionDetector( listenerDefinitionDetector );
	}

	/**
	 * To add custom support for your mocking tool.
	 * <p></p>
	 * Usually you don't need to use this.
	 * Only if you want to register custom implementation of {@link ListenerRegistrationHandler} for your mocking tool.
	 * <p></p>
	 * Important: Registered handlers are kept statically, so it will stick between your unit tests.
	 * Make sure you reset it if needed (see {@link #reset()}).
	 *
	 * @param registrationHandler implementation of {@link ListenerRegistrationHandler} for your mocking tool.
	 * @see MockingToolsRegistry#registerListenerRegistrationHandler
	 * @see #reset()
	 */
	public static void registerListenerRegistrationHandler( @NonNull ListenerRegistrationHandler registrationHandler ) {
		MOCKING_TOOLS_REGISTRY.registerListenerRegistrationHandler( registrationHandler );
	}


	/**
	 * To get {@link NotifierSettingsUpdater} for update statically store {@link NotifierSettings}.
	 * <p>
	 * Example:
	 * <pre class="code"><code class="java">
	 * MockoborContext.updateNotifierSettings()
	 *     .ignoreListenersInterfaces()
	 *     .lenientListenerListCheck();
	 * </code></pre>
	 * <p></p>
	 * Important: Settings are kept statically, so it will stick between your unit tests.
	 * Make sure you reset it if needed (see {@link #reset()}).
	 *
	 * @return updater for settings used to create a new listener notifiers.
	 * @see #reset()
	 */
	public static @NonNull NotifierSettingsUpdater updateNotifierSettings() {
		return notifierSettingsImpl;
	}


	/**
	 * To remove/reset all:<ul>
	 *     <li>custom listener definition detectors (added with {@link #registerListenerDefinitionDetector})</li>
	 *     <li>custom support for your mocking tool (added with {@link #registerListenerRegistrationHandler})</li>
	 *     <li>custom global settings (set with {@link #updateNotifierSettings()})</li>
	 * </ul>
	 * <p><br>
	 * Usually you don't need to call it.
	 * Only if you want to reset changes made by settings or by registration of custom detectors or mocking tools.
	 *
	 * @see ListenerDetectorsRegistry#reset()
	 * @see MockingToolsRegistry#reset()
	 */
	public static void reset() {
		LISTENER_DETECTORS_REGISTRY.reset();
		MOCKING_TOOLS_REGISTRY.reset();
		notifierSettingsImpl = NotifierSettingsImpl.createDefaultSettings();
	}
}

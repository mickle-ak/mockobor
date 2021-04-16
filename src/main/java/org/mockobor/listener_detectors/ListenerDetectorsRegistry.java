package org.mockobor.listener_detectors;

import lombok.NonNull;

import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;


/**
 * To manage listener definition detectors.
 * <p><br>
 * Out of the box (per default) follow detectors are registered (in processing order):<ol>
 * <li>{@link PropertyChangeDetector} for beans that support bound properties (see {@link PropertyChangeSupport}).</li>
 * <li>{@link TypicalJavaListenerDetector} for typical Java listener.</li>
 * <li>{@link ObservableDetector} for {@link Observer}/{@link Observable}.</li>
 * </ol>
 * <p><br>
 * Usually you don't need to use this.
 * Only if you want to register custom listener definition detectors.
 */
public interface ListenerDetectorsRegistry {


	/**
	 * To return list of all registered detectors in correct order (LIFO).
	 *
	 * @return immutable list of all registered detectors in correct order
	 */
	@NonNull Collection<ListenerDefinitionDetector> getDetectors();


	/**
	 * To add custom listener definition detector.
	 * <p><br>
	 * A new registered detector have priority over previously registered detectors (inclusive detectors registered per default).
	 * <p><br>
	 * Usually you don't need to call it. Only if you want to register custom listener definition detectors.
	 *
	 * @param listenerDefinitionDetector custom listener definition detector to register
	 */
	void registerListenerDefinitionDetector( @NonNull ListenerDefinitionDetector listenerDefinitionDetector );


	/**
	 * To remove all registered custom listener definition detectors.
	 * <p><br>
	 * Usually you don't need to call it. Only if you want to reset changes made by registration of custom listener definition detectors.
	 */
	void reset();
}
